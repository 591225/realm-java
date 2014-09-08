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

package io.realm.typed;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.Calendar;
import java.util.Date;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.Mixed;
import io.realm.TableView.Order;
import io.realm.test.AllTypesTable;
import io.realm.test.AllTypesView;
import io.realm.test.TestEmployeeTable;
import io.realm.test.TestEmployeeView;

public class ViewTest {

    protected static final String NAME0 = "John";
    protected static final String NAME1 = "Nikolche";
    protected static final String NAME2 = "Johny";

    protected TestEmployeeTable employees;

    @BeforeMethod
    public void init() {
        Date date = new Date(1234567890);
        employees = new TestEmployeeTable();

        employees.add(NAME0, "Doe", 10000, true, new byte[] { 1, 2, 3 }, date, "extra", null);
        employees.add(NAME2, "B. Good", 10000, true, new byte[] { 1 }, date, true, null);
        employees.insert(1, NAME1, "Mihajlovski", 30000, false, new byte[] { 1 }, date, 1234, null);
        employees.add("NoName", "Test Mixed Date", 1, true, new byte[] { 1 }, date, new Date(123456789), null);
        employees.add("NoName", "Test Mixed Binary", 1, true, new byte[] { 1, 2, 3 }, date, new byte[] { 3, 2, 1 },
                null);
    }

    @Test
    public void sortViewEmployees(){
        TestEmployeeView view = employees.where().findAll();
        view.salary.sort();
        assertEquals(1, view.get(0).getSalary());

        view.salary.sort(Order.ascending);
        assertEquals(1, view.get(0).getSalary());

        view.salary.sort(Order.descending);
        assertEquals(10000, view.get(1).getSalary());
    }

    @Test
    public void sortViewAllTypes(){
        AllTypesTable t = new AllTypesTable();

        Calendar cal1 = Calendar.getInstance();

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.YEAR, 1);

        t.add(true, cal1.getTime(), 1.0d, 1, 1l, 1.0f, "s", new byte[] {1}, null, new Mixed("s"));
        t.add(false, cal2.getTime(), 2.0d, 2, 2l, 2.0f, "ss", new byte[] {1,2}, null, new Mixed("ss"));
        t.add(false, cal3.getTime(), 3.0d, 3, 3l, 3.0f, "sss", new byte[] {1,2,3}, null, new Mixed("sss"));

        AllTypesView v = t.where().findAll();

        // boolean supported
        v.boolCol.sort();
        assertEquals(false, v.get(0).getBoolCol());
        v.boolCol.sort(Order.ascending);
        assertEquals(false, v.get(0).getBoolCol());
        v.boolCol.sort(Order.descending);
        assertEquals(true, v.get(0).getBoolCol());

        // Date supported
        v.dateCol.sort();
        assertEquals(cal1.getTime().getTime()/1000, v.get(0).getDateCol().getTime()/1000);
        v.dateCol.sort(Order.ascending);
        assertEquals(cal1.getTime().getTime()/1000, v.get(0).getDateCol().getTime()/1000);
        v.dateCol.sort(Order.descending);
        assertEquals(cal3.getTime().getTime()/1000, v.get(0).getDateCol().getTime()/1000);

        /*
        // Double column NOT supported
        try {   v.sort(2);                      fail("sort on double not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(2, Order.ascending);     fail("sort on double not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(2, Order.descending);    fail("sort on double not supported"); } catch (IllegalArgumentException e) { }
        */


        // int column supported
        v.intCol.sort();
        assertEquals(1, v.get(0).getIntCol());
        v.intCol.sort(Order.ascending);
        assertEquals(1, v.get(0).getIntCol());
        v.intCol.sort(Order.descending);
        assertEquals(3, v.get(0).getIntCol());

        // long supported
        v.intCol.sort();
        assertEquals(1l, v.get(0).getLongCol());
        v.intCol.sort(Order.ascending);
        assertEquals(1l, v.get(0).getLongCol());
        v.intCol.sort(Order.descending);
        assertEquals(3l, v.get(0).getLongCol());

        /*
        // float column NOT supported
        try {   v.sort(5);                      fail("sort on float not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(5, Order.ascending);     fail("sort on float not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(5, Order.descending);    fail("sort on float not supported"); } catch (IllegalArgumentException e) { }

        // String column NOT supported
        try {   v.sort(6);                      fail("sort on String not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(6, Order.ascending);     fail("sort on String not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(6, Order.descending);    fail("sort on String not supported"); } catch (IllegalArgumentException e) { }

        // byte[] column NOT supported
        try {   v.sort(7);                      fail("sort on byte[] not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(7, Order.ascending);     fail("sort on byte[] not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(7, Order.descending);    fail("sort on byte[] not supported"); } catch (IllegalArgumentException e) { }

        // subtable column NOT supported
        try {   v.sort(8);                      fail("sort on subtable not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(8, Order.ascending);     fail("sort on subtable not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(8, Order.descending);    fail("sort on subtable not supported"); } catch (IllegalArgumentException e) { }

        // Mixed column NOT supported
        try {   v.sort(9);                      fail("sort on Mixed not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(9, Order.ascending);     fail("sort on Mixed not supported"); } catch (IllegalArgumentException e) { }
        try {   v.sort(9, Order.descending);    fail("sort on Mixed not supported"); } catch (IllegalArgumentException e) { }
        */
    }
}
