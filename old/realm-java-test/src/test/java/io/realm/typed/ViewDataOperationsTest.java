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

import java.util.Date;

import io.realm.Mixed;
import io.realm.WriteTransaction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.realm.ReadTransaction;
import io.realm.SharedGroup;
import io.realm.test.TestEmployeeQuery;
import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeTable;
import io.realm.test.TestEmployeeView;

@Test
public class ViewDataOperationsTest extends AbstractDataOperationsTest {

    TestEmployeeView employees;

    @Override
    protected AbstractTableOrView<TestEmployeeRow, TestEmployeeView, TestEmployeeQuery> getEmployees() {
        return employees;
    }

    @BeforeMethod
    public void init() {
        TestEmployeeTable employeesTable = getEmployeeTable();
        employees = employeesTable.where().findAll();
    }

    @Test
    public void shouldPrint() {
        super.shouldPrintData("TestEmployeeView");
    }




    @Test
    public void setValuesOnViewInReadTransactionShouldFail() {

        SharedGroup group = new SharedGroup("viewTest.realm");

        // Create table if it does not exists
        WriteTransaction wt = group.beginWrite();
        try {
            TestEmployeeTable t = new TestEmployeeTable(wt);
            t.add("NoName", "Test Mixed Binary", 1, true, new byte[] { 1, 2, 3 }, new Date(), new byte[] { 3, 2, 1 },null);
            wt.commit();
        } catch (Throwable t){
            wt.rollback();
        }

        ReadTransaction rt = group.beginRead();

        try {
            TestEmployeeTable t = new TestEmployeeTable(rt);
            TestEmployeeView view = t.where().findAll();

            try {  view.get(0).setFirstName("new string");          assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).setLastName("new last name");        assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).setExtra(new Mixed(true));           assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).setBirthdate(new Date());            assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).setDriver(false);                    assert(false); } catch (IllegalStateException e){ }
            try {  view.get(0).setPhoto(null);                      assert(false); } catch (IllegalStateException e){ }

        } finally {
            rt.endRead();
        }
    }
}
