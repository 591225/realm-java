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
import static org.testng.AssertJUnit.assertNull;

import java.util.Date;

import io.realm.test.EmployeesFixture;
import org.testng.annotations.Test;

import io.realm.test.TestEmployeeRow;
import io.realm.test.TestEmployeeView;

public class ViewColumnsTest extends AbstractTest {

    @Test
    public void shouldFindFirstRecordByColumnValue()
            throws IllegalAccessException {
        TestEmployeeRow record;

        record = employeesView.firstName
                .findFirst(EmployeesFixture.EMPLOYEES[1].firstName);
        assertEquals(1, record.getPosition());

        record = employeesView.salary
                .findFirst(EmployeesFixture.EMPLOYEES[0].salary);
        assertEquals(0, record.getPosition());

        record = employeesView.salary.findFirst(12345);
        assertNull(record);

        record = employeesView.driver
                .findFirst(EmployeesFixture.EMPLOYEES[0].driver);
        assertEquals(0, record.getPosition());

        record = employeesView.driver
                .findFirst(EmployeesFixture.EMPLOYEES[1].driver);
        assertEquals(1, record.getPosition());

        record = employeesView.birthdate
                .findFirst(EmployeesFixture.EMPLOYEES[1].birthdate);
        assertEquals(1, record.getPosition());

        record = employeesView.birthdate
                .findFirst(EmployeesFixture.EMPLOYEES[2].birthdate);
        assertEquals(2, record.getPosition());

        record = employeesView.birthdate.findFirst(new Date(12345));
        assertNull(record);
    }

    @Test
    public void shouldFindAllRecordsByColumnValue()
            throws IllegalAccessException {
        TestEmployeeView view = null;

        view = employeesView.firstName
                .findAll(EmployeesFixture.EMPLOYEES[1].firstName);
        assertEquals(1, view.size());

        view = employeesView.salary
                .findAll(EmployeesFixture.EMPLOYEES[0].salary);
        assertEquals(2, view.size());

        view = employeesView.salary.findAll(12345);
        assertEquals(0, view.size());

        view = employeesView.driver.findAll(false);
        assertEquals(1, view.size());

        view = employeesView.driver.findAll(true);
        assertEquals(2, view.size());

        view = employeesView.birthdate
                .findAll(EmployeesFixture.EMPLOYEES[2].birthdate);
        assertEquals(1, view.size());

        view = employeesView.birthdate
                .findAll(EmployeesFixture.EMPLOYEES[1].birthdate);
        assertEquals(1, view.size());

        view = employeesView.birthdate.findAll(new Date(0));
        assertEquals(0, view.size());
    }

    @Test
    public void shouldAggregateColumnValue() {
        assertEquals(EmployeesFixture.EMPLOYEES[0].salary,
                employeesView.salary.minimum());

        assertEquals(EmployeesFixture.EMPLOYEES[1].salary,
                employeesView.salary.maximum());

        long sum = EmployeesFixture.EMPLOYEES[0].salary
                + EmployeesFixture.EMPLOYEES[1].salary
                + EmployeesFixture.EMPLOYEES[2].salary;
        assertEquals(sum, employeesView.salary.sum());

        assertEquals(sum / 3.0, employees.salary.average(), 0.00001);
    }

    @Test
    public void shouldAddValueToWholeColumn() {
        employeesView.salary.adjust(123);
        for (int i = 0; i < EmployeesFixture.EMPLOYEES.length; ++i)
            assertEquals(EmployeesFixture.EMPLOYEES[i].salary + 123,
                    employeesView.get(i).getSalary());
    }




}
