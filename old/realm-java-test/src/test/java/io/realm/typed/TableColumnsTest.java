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


import static org.testng.AssertJUnit.*;

import java.util.Date;

import io.realm.Mixed;
import io.realm.test.EmployeesFixture;
import io.realm.test.ExtraTests;
import io.realm.test.PhoneData;
import org.testng.annotations.Test;

import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeView;
import io.realm.test.TestPhoneTable;

public class TableColumnsTest extends AbstractTest {

    @Test
    public void shouldFindFirstRecordByColumnValue()
            throws IllegalAccessException {
        TestEmployeeRow record = null;

        record = employees.firstName
                .findFirst(EmployeesFixture.EMPLOYEES[1].firstName);
        assertEquals(1, record.getPosition());

        record = employees.salary
                .findFirst(EmployeesFixture.EMPLOYEES[0].salary);
        assertEquals(0, record.getPosition());

        record = employees.salary.findFirst(12345);
        assertNull(record);

        record = employees.driver
                .findFirst(EmployeesFixture.EMPLOYEES[0].driver);
        assertEquals(0, record.getPosition());

        record = employees.driver
                .findFirst(EmployeesFixture.EMPLOYEES[1].driver);
        assertEquals(1, record.getPosition());

        record = employees.birthdate
                .findFirst(EmployeesFixture.EMPLOYEES[1].birthdate);
        assertEquals(1, record.getPosition());

        record = employees.birthdate
                .findFirst(EmployeesFixture.EMPLOYEES[2].birthdate);
        assertEquals(2, record.getPosition());

        record = employees.birthdate.findFirst(new Date(12345));
        assertNull(record);

    }

    @Test
    public void shouldFindAllRecordsByColumnValue()
            throws IllegalAccessException {
        TestEmployeeView view = null;
        view = employees.firstName
                .findAll(EmployeesFixture.EMPLOYEES[1].firstName);
        assertEquals(1, view.size());

        view = employees.salary.findAll(EmployeesFixture.EMPLOYEES[0].salary);
        assertEquals(2, view.size());

        view = employees.salary.findAll(12345);
        assertEquals(0, view.size());

        view = employees.driver.findAll(false);
        assertEquals(1, view.size());

        view = employees.driver.findAll(true);
        assertEquals(2, view.size());

        view = employees.birthdate
                .findAll(EmployeesFixture.EMPLOYEES[2].birthdate);
        assertEquals(1, view.size());

        view = employees.birthdate
                .findAll(EmployeesFixture.EMPLOYEES[1].birthdate);
        assertEquals(1, view.size());

        view = employees.birthdate.findAll(new Date(0));
        assertEquals(0, view.size());
    }

    @Test()
    public void shouldAggregateColumnValue() {
        assertEquals(EmployeesFixture.EMPLOYEES[0].salary,
                employees.salary.minimum());

        assertEquals(EmployeesFixture.EMPLOYEES[1].salary,
                employees.salary.maximum());

        long sum = EmployeesFixture.EMPLOYEES[0].salary
                + EmployeesFixture.EMPLOYEES[1].salary
                + EmployeesFixture.EMPLOYEES[2].salary;
        assertEquals(sum, employees.salary.sum());

        assertEquals(sum / 3.0, employees.salary.average(), 0.00001);
    }

    @Test
    public void shouldAddValueToWholeColumn() {
        employees.salary.adjust(123);
        for (int i = 0; i < EmployeesFixture.EMPLOYEES.length; ++i)
            assertEquals(EmployeesFixture.EMPLOYEES[i].salary + 123, employees
                    .get(i).getSalary());
    }

    @Test
    public void shouldGetAllColumnValues() {
        ExtraTests.assertArrayEquals(EmployeesFixture.getAll(0),
                employees.firstName.getAll());
        ExtraTests.assertArrayEquals(EmployeesFixture.getAll(1),
                employees.lastName.getAll());
        ExtraTests.assertArrayEquals(EmployeesFixture.getAll(2), employees.salary.getAll());
        ExtraTests.assertArrayEquals(EmployeesFixture.getAll(3), employees.driver.getAll());
        byte[][] bActual = employees.photo.getAll();
        Object[] bExpected = EmployeesFixture.getAll(4);
        for(int i = 0; i < bActual.length; i++) {
            assertEquals(((java.nio.ByteBuffer)(bExpected[i])).array(), bActual[i]);
        }
        ExtraTests.assertDateArrayEquals(EmployeesFixture.getAll(5), employees.birthdate.getAll());
        ExtraTests.assertArrayEquals(EmployeesFixture.getAll(6), employees.extra.getAll());

        TestPhoneTable[] phoneTables = employees.phones.getAll();
        assertEquals(EmployeesFixture.PHONES.length, phoneTables.length);

        for (int i = 0; i < phoneTables.length; i++) {
            PhoneData[] phones = EmployeesFixture.PHONES[i];
            assertEquals(phones.length, phoneTables[i].size());
            for (int j = 0; j < phones.length; j++) {
                assertEquals(phones[j].type, phoneTables[i].get(j).getType()
                );
                assertEquals(phones[j].number,
                        phoneTables[i].get(j).getNumber());
            }
        }
    }

    @Test
    public void shouldSetAllColumnValues() {
        employees.firstName.setAll("A");
        assertSameArrayElement("A", employees.firstName.getAll());

        employees.lastName.setAll("B");
        assertSameArrayElement("B", employees.lastName.getAll());

        Long num = 12345L;
        employees.salary.setAll(num);
        assertSameArrayElement(num, employees.salary.getAll());

        employees.driver.setAll(true);
        assertSameArrayElement(true, employees.driver.getAll());

        byte[] bArray = new byte[] { 10, 20 };
        employees.photo.setAll(bArray);
        for(byte[] bActual : employees.photo.getAll()) {
            assertEquals(bArray, bActual);
        }


        Date date = new Date(13579);
        employees.birthdate.setAll(date);
        for (Date d : employees.birthdate.getAll()) {
            // Dates are truncated to secs
            assertEquals(date.getTime()/1000, d.getTime()/1000);
        }

        Mixed extra = Mixed.mixedValue("extra");
        employees.extra.setAll(extra);
        assertSameArrayElement(extra, employees.extra.getAll());
    }

    private void assertSameArrayElement(Object expected, Object[] arr) {
        for (Object element : arr) {
            assertEquals(expected, element);
        }
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldntGetDirectColumnValue() {
        employees.firstName.get();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void shouldntSetDirectColumnValue() {
        employees.firstName.set("x");
    }

}
