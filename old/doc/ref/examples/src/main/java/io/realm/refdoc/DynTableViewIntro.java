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

// @@Example: ex_java_dyn_table_view_intro @@
package io.realm.refdoc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import io.realm.*;

public class DynTableViewIntro {

    public static void main(String[] args) throws FileNotFoundException  {
        // @@Show@@
        // Create a new table
        Table table = new Table();

        // Specify the column types and names
        table.addColumn(ColumnType.INTEGER, "ID");
        table.addColumn(ColumnType.STRING, "City");

        // Add data to the table
        table.add(100, "Washington");
        table.add(200, "Los Angeles");
        table.add(300, "New York");

        // Create a query object from the table without any filters
        // and execute it to retrieve a table view
        TableView view = table.where().findAll();

        // Remove the first row from the view and thereby also the original table
        // and check that the number of rows in the original table is 2
        view.remove(0);
        Assert(table.size() == 2);

        // Change the value of column 1, row 1 to 'London'.
        // The changes are reflected in the original table
        view.setString(1, 1, "London");
        Assert(table.getString(1, 1).equals("London"));

        // Simple aggregations
        Assert(view.sumLong(0) == 500);
        Assert(view.maximumLong(0) == 300);
        Assert(view.maximumLong(0) == 300);
        Assert(view.averageLong(0) == 250);


        // Get JSON representation of the data in the view
        // and print it using e.g. a PrintWriter object
        PrintWriter out = new PrintWriter("fromServlet");
        out.print(view.toJson());
        out.close();
        // @@EndShow@@
    }

    static void Assert(boolean check) {
        if (!check) {
            throw new RuntimeException();
        }
    }
}
// @@EndExample@@
