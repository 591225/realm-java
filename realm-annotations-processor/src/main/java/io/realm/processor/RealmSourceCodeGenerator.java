/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.processor;

import com.squareup.javawriter.JavaWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class RealmSourceCodeGenerator {

    private class FieldInfo {
        public String fieldName;
        public String fieldId;
        public String columnType;
        public Element fieldElement;

        public FieldInfo(String fieldName, String fieldId, String columnType, Element fieldElement) {
            this.fieldId = fieldId;
            this.columnType = columnType;
            this.fieldElement = fieldElement;
            this.fieldName = fieldName;
        }
    }

    private enum GeneratorStates {
        PACKAGE,
        CLASS,
        METHODS,
    }

    private JavaWriter writer = null;
    private String className = null;
    private String packageName = null;
    private GeneratorStates generatorState = GeneratorStates.PACKAGE;
    private String errorMessage = "";
    private List<FieldInfo> fields = new ArrayList<FieldInfo>();

    private void setError(String message) {
        errorMessage = message;
    }

    public String getError() {
        return errorMessage;
    }

    private String convertSimpleTypesToObject(String typeName) {
        if (typeName.compareTo("int") == 0) {
            typeName = "Integer";
        } else if (typeName.compareTo("long") == 0 || typeName.compareTo("float") == 0 ||
                typeName.compareTo("double") == 0 || typeName.compareTo("boolean") == 0) {
            typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
        }

        return typeName;
    }

    private String convertTypesToColumnType(String typeName) {
        if (typeName.compareTo("String") == 0) {
            typeName = "ColumnType.STRING";
        } else if (typeName.compareTo("Long") == 0 || typeName.compareTo("Integer") == 0) {
            typeName = "ColumnType.INTEGER";
        } else if (typeName.compareTo("Float") == 0) {
            typeName = "ColumnType.FLOAT";
        } else if (typeName.compareTo("Double") == 0) {
            typeName = "ColumnType.DOUBLE";
        } else if (typeName.compareTo("Boolean") == 0) {
            typeName = "ColumnType.BOOLEAN";
        } else if (typeName.compareTo("Date") == 0) {
            typeName = "ColumnType.DATE";
        } else if (typeName.compareTo("byte[]") == 0) {
            typeName = "ColumnType.BINARY";
        }

        return typeName;
    }

    private boolean checkState(GeneratorStates checkState) {
        if (writer == null) {
            setError("No output writer has been defined");
            return false;
        }

        if (generatorState != checkState) {
            setError("Annotations received in wrong order");
            return false;
        }

        return true;
    }

    public void setBufferedWriter(BufferedWriter bw) {
        writer = new JavaWriter(bw);
    }

    public boolean setPackageName(String packageName) {
        if (!checkState(GeneratorStates.PACKAGE)) return false;

        this.packageName = packageName;

        generatorState = GeneratorStates.CLASS;
        return true;
    }

    private void emitPackage() throws IOException {
        writer.emitPackage(packageName)
                .emitEmptyLine()
                .emitImports(
                        "io.realm.internal.ColumnType",
                        "io.realm.internal.Table",
                        "io.realm.internal.ImplicitTransaction",
                        "io.realm.internal.Row")
                .emitEmptyLine();
    }

    public boolean setClassName(String className) {
        if (!checkState(GeneratorStates.CLASS)) return false;

        this.className = className;

        generatorState = GeneratorStates.METHODS;
        return true;
    }

    private void emitClass() throws IOException {
        writer.beginType(packageName + "." + className + "RealmProxy", "class",
                EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), className).emitEmptyLine();
    }

    public boolean setField(String fieldName, Element fieldElement) {
        if (!checkState(GeneratorStates.METHODS)) return false;

        String fieldId = "index_" + fieldName;

        String shortType = convertSimpleTypesToObject(fieldElement.asType().toString());
        shortType = shortType.substring(shortType.lastIndexOf(".") + 1);

        fields.add(new FieldInfo(fieldName, fieldId, convertTypesToColumnType(shortType), fieldElement));

        return true;
    }

    public void emitFields() throws IOException {

        for (FieldInfo field : fields) {
            String originalType = field.fieldElement.asType().toString();
            String fullType = convertSimpleTypesToObject(originalType);
            String shortType = fullType.substring(fullType.lastIndexOf(".") + 1);

            String returnCast = "";
            String camelCaseFieldName = Character.toUpperCase(field.fieldName.charAt(0)) + field.fieldName.substring(1);

            if (originalType.compareTo("int") == 0) {
                shortType = "Long";
                returnCast = "(" + originalType + ")";
            } else if (shortType.compareTo("Integer") == 0) {
                shortType = "Long";
                returnCast = "(int)";
            } else if (shortType.compareTo("byte[]") == 0) {
                shortType = "BinaryByteArray";
                returnCast = "(byte[])";
            }

            String getterStmt = "return " + returnCast + "row.get" + shortType + "( " + field.fieldId + " )";

            String setterStmt = "row.set" + shortType + "( " + field.fieldId + ", value )";

            if (!field.fieldElement.asType().getKind().isPrimitive())
            {
                if (originalType.compareTo("java.lang.String") != 0 &&
                	originalType.compareTo("java.lang.Long") != 0 &&
                	originalType.compareTo("java.lang.Integer") != 0 &&
                	originalType.compareTo("java.lang.Float") != 0 &&
                	originalType.compareTo("java.lang.Double") != 0 &&
                	originalType.compareTo("java.lang.Boolean") != 0 &&
                	originalType.compareTo("java.util.Date") != 0 &&
                	originalType.compareTo("byte[]") != 0) {
                	
                	// We now know this is a type derived from RealmObject - 
                	// this has already been checked in the RealmProcessor
                	setterStmt = "if (value != null) {row.setLink("+field.fieldId+", value.realmGetRow().getIndex());}";
                	getterStmt = "long link = row.getLink("+field.fieldId+");\n"+
                	"Row newRow = getTransaction().getTable(\""+shortType+"\").getRow(link);"+
                	"\n"+shortType+"RealmProxy obj = new "+shortType+"RealmProxy();"+
                	"\nobj.realmSetRow(row);"+
                	"\nobj.setTransaction(getTransaction());"+
                	"\nreturn obj";
                    field.columnType = "ColumnType.LINK";
                }
            }
            
            writer.emitField("int", field.fieldId, EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));

            writer.emitAnnotation("Override").beginMethod(originalType, "get" + camelCaseFieldName, EnumSet.of(Modifier.PUBLIC))
                    .emitStatement(getterStmt)
                    .endMethod();

            writer.emitAnnotation("Override").beginMethod("void", "set" + camelCaseFieldName, EnumSet.of(Modifier.PUBLIC),
                    originalType, "value")
                    .emitStatement(setterStmt)
                    .endMethod().emitEmptyLine();
        }
    }

    public boolean generate() throws IOException {

    	// Set source code indent to 4 spaces
        writer.setIndent("    ");

        // Emit java writer code in sections: 
        
        //   1. Package Header and imports
        emitPackage();
        
        //   2. class definition
        emitClass();
        
        //   3. public setters and getters for each field
        emitFields();

        // Generate initTable method, which is used to create the datqbase table
        writer.beginMethod("Table", "initTable", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                "ImplicitTransaction", "transaction").
                beginControlFlow("if(!transaction.hasTable(\"" + this.className + "\"))").
                emitStatement("Table table = transaction.getTable(\"" + this.className + "\")");

        // For each field generate corresponding table index constant
        for (int index = 0; index < fields.size(); ++index) {
            FieldInfo field = fields.get(index);
            String fieldName = field.fieldId.substring("index_".length());
            writer.emitStatement(field.fieldId + " = " + Integer.toString(index));
            writer.emitStatement("table.addColumn( %s, \"%s\" )", field.columnType, fieldName.toLowerCase(Locale.getDefault()));
        }

        writer.emitStatement("return table");
        writer.endControlFlow();
        writer.emitStatement("return transaction.getTable(\"" + this.className + "\")");
        writer.endMethod().emitEmptyLine();

        // End the class definition 
        writer.endType();
        writer.close();

        fields.clear();

        generatorState = GeneratorStates.PACKAGE;

        return true;
    }
}
