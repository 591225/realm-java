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
package io.realm;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import io.realm.entities.AllTypes;
import io.realm.entities.AnnotationTypes;
import io.realm.entities.Dog;
import io.realm.exceptions.RealmException;

import static io.realm.internal.test.ExtraTests.assertArrayEquals;

public class RealmJsonTest extends AndroidTestCase {

    protected Realm testRealm;

    @Override
    protected void setUp() throws Exception {
        Realm.deleteRealmFile(getContext());
        testRealm = Realm.getInstance(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        testRealm.close();
    }

    private InputStream loadJsonFromAssets(String file) {
        AssetManager assetManager = getContext().getAssets();
        InputStream input = null;
        try {
            input = assetManager.open(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            return input;
        }
    }

    public void testCreateObjectFromJson_nullObject() {
        testRealm.createObjectFromJson(AllTypes.class, (JSONObject) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testCreateObjectFromJson_nullArray() {
        testRealm.createAllFromJson(AllTypes.class, (JSONArray) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());

    }

    public void testCreateObjectFromJson_allSimpleObjectAllTypes() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "String");
        json.put("columnLong", 1l);
        json.put("columnFloat", 1.23f);
        json.put("columnDouble", 1.23d);
        json.put("columnBoolean", true);
        json.put("columnBinary", new String(Base64.encode(new byte[] {1,2,3}, Base64.DEFAULT)));

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();

        // Check that all primitive types are imported correctly
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    public void testCreateObjectFromJson_dateAsLong() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", 1000L); // Realm operates at seconds level granularity

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJson_dateAsString() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnDate", "/Date(1000)/");

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJson_childObject() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONObject dogObject = new JSONObject();
        dogObject.put("name", "Fido");
        allTypesObject.put("columnRealmObject", dogObject);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    public void testCreateObjectFromJson_childObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONObject dog1 = new JSONObject(); dog1.put("name", "Fido-1");
        JSONObject dog2 = new JSONObject(); dog2.put("name", "Fido-2");
        JSONObject dog3 = new JSONObject(); dog3.put("name", "Fido-3");
        JSONArray dogList = new JSONArray();
        dogList.put(dog1);
        dogList.put(dog2);
        dogList.put(dog3);

        allTypesObject.put("columnRealmList", dogList);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(3, obj.getColumnRealmList().size());
        assertEquals("Fido-3", obj.getColumnRealmList().get(2).getName());
    }

    public void testCreateObjectFromJson_emptyChildObjectList() throws JSONException {
        JSONObject allTypesObject = new JSONObject();
        JSONArray dogList = new JSONArray();

        allTypesObject.put("columnRealmList", dogList);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, allTypesObject);
        testRealm.commitTransaction();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testCreateObjectFromJsonString_simpleObject() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObjectFromJson(Dog.class, "{ name: \"Foo\" }");
        testRealm. commitTransaction();

        assertEquals("Foo", dog.getName());
        assertEquals("Foo", testRealm.allObjects(Dog.class).first().getName());
    }


    public void testCreateObjectFromJsonString_faultyJsonThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.createObjectFromJson(Dog.class, "{ name \"Foo\" }");
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.commitTransaction();
        }

        fail("Faulty JSON should result in a RealmException");
    }


    public void testCreateObjectFromJsonString_null() {
        testRealm.beginTransaction();
        Dog dog = testRealm.createObjectFromJson(Dog.class, (String) null);
        testRealm.commitTransaction();

        assertNull(dog);
        assertEquals(0, testRealm.allObjects(Dog.class).size());
    }

    public void testCreateAllFromJsonArray_empty() {
        JSONArray array = new JSONArray();
        testRealm.beginTransaction();
        testRealm.createAllFromJson(AllTypes.class, array);
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testCreateAllFromJsonArray() throws JSONException {
        JSONObject dog1 = new JSONObject(); dog1.put("name", "Fido-1");
        JSONObject dog2 = new JSONObject(); dog2.put("name", "Fido-2");
        JSONObject dog3 = new JSONObject(); dog3.put("name", "Fido-3");
        JSONArray dogList = new JSONArray();
        dogList.put(dog1);
        dogList.put(dog2);
        dogList.put(dog3);

        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, dogList);
        testRealm.commitTransaction();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    public void testCreateObjectFromJson_nullValues() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", null);
        json.put("columnLong", null);
        json.put("columnFloat", null);
        json.put("columnDouble", null);
        json.put("columnBoolean", null);
        json.put("columnBinary", null);
        json.put("columnDate", null);
        json.put("columnRealmObject", null);
        json.put("columnRealmList", null);

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, json);
        testRealm.commitTransaction();
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();

        // Check that all primitive types are imported correctly
        assertEquals("", obj.getColumnString());
        assertEquals(0L, obj.getColumnLong());
        assertEquals(0f, obj.getColumnFloat());
        assertEquals(0d, obj.getColumnDouble());
        assertEquals(false, obj.isColumnBoolean());
        assertEquals(new Date(0), obj.getColumnDate());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }

    // Test that given an exception everything up to the exception is saved
    public void testCreateObjectFromJson_jsonException() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("columnString", "Foo");
        json.put("columnDate", "Boom");

        try {
            testRealm.beginTransaction();
            testRealm.createObjectFromJson(AllTypes.class, json);
        } catch (RealmException e) {
            // Ignore
        } finally {
            testRealm.commitTransaction();
        }

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Foo", obj.getColumnString());
        assertEquals(new Date(0), obj.getColumnDate());
    }

    public void testCreateObjectFromJson_respectIgnoredFields() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("indexString", "Foo");
        json.put("notIndexString", "Bar");
        json.put("ignoreString", "Baz");

        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AnnotationTypes.class, json);
        testRealm.commitTransaction();

        AnnotationTypes annotationsObject = testRealm.allObjects(AnnotationTypes.class).first();
        assertEquals("Foo", annotationsObject.getIndexString());
        assertEquals(null, annotationsObject.getIgnoreString());
    }

    public void testCreateAllFromJsonStringArray_simpleArray() {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, "[{ name: \"Foo\" }, { name: \"Bar\" }]");
        testRealm. commitTransaction();

        assertEquals(2, testRealm.allObjects(Dog.class).size());
    }

    public void testCreateAllFromJsonStringArray_faultyJsonThrows() {
        testRealm.beginTransaction();
        try {
            testRealm.createAllFromJson(Dog.class, "[{ name : \"Foo\" ]");
        } catch (RealmException e) {
            return;
        } finally {
            testRealm.commitTransaction();
        }

        fail("Faulty JSON should result in a RealmException");
    }


    public void testCreateAllFromJsonStringArray_null() {
        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, (String) null);
        testRealm.commitTransaction();

        assertEquals(0, testRealm.allObjects(Dog.class).size());
    }

   public void testCreateAllFromJsonStream_null() throws IOException {
        testRealm.createAllFromJson(AllTypes.class, (InputStream) null);
        assertEquals(0, testRealm.allObjects(AllTypes.class).size());
    }

    public void testCreateObjectFromJsonStream_allSimpleTypes() throws IOException {
        InputStream in = loadJsonFromAssets("all_simple_types.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("String", obj.getColumnString());
        assertEquals(1l, obj.getColumnLong());
        assertEquals(1.23f, obj.getColumnFloat());
        assertEquals(1.23d, obj.getColumnDouble());
        assertEquals(true, obj.isColumnBoolean());
        assertArrayEquals(new byte[]{1, 2, 3}, obj.getColumnBinary());
    }

    public void testCreateObjectFromJsonStream_dateAsLong() throws IOException {
        InputStream in = loadJsonFromAssets("date_as_long.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJsonStream_dateAsString() throws IOException {
        InputStream in = loadJsonFromAssets("date_as_string.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(new Date(1000), obj.getColumnDate());
    }

    public void testCreateObjectFromJsonStream_childObject() throws IOException {
        InputStream in = loadJsonFromAssets("single_child_object.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("Fido", obj.getColumnRealmObject().getName());
    }

    public void testCreateObjectFromJsonStream_emptyChildObjectList() throws IOException {
        InputStream in = loadJsonFromAssets("realmlist_empty.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals(0, obj.getColumnRealmList().size());
    }

    public void testCreateObjectFromJsonStream_childObjectList() throws IOException {
        InputStream in = loadJsonFromAssets("realmlist.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }

    public void testCreateObjectFromJsonStream_array() throws IOException {
        InputStream in = loadJsonFromAssets("array.json");

        testRealm.beginTransaction();
        testRealm.createAllFromJson(Dog.class, in);
        testRealm.commitTransaction();

        assertEquals(3, testRealm.allObjects(Dog.class).size());
        assertEquals(1, testRealm.where(Dog.class).equalTo("name", "Fido-3").findAll().size());
    }


    public void testCreateObjectFromJsonStream_nullValues() throws IOException {
        InputStream in = loadJsonFromAssets("all_types_null.json");
        testRealm.beginTransaction();
        testRealm.createObjectFromJson(AllTypes.class, in);
        testRealm.commitTransaction();
        in.close();

        // Check that all primitive types are imported correctly
        AllTypes obj = testRealm.allObjects(AllTypes.class).first();
        assertEquals("", obj.getColumnString());
        assertEquals(0L, obj.getColumnLong());
        assertEquals(0f, obj.getColumnFloat());
        assertEquals(0d, obj.getColumnDouble());
        assertEquals(false, obj.isColumnBoolean());
        assertEquals(new Date(0), obj.getColumnDate());
        assertArrayEquals(new byte[0], obj.getColumnBinary());
        assertNull(obj.getColumnRealmObject());
        assertEquals(0, obj.getColumnRealmList().size());
    }
}
