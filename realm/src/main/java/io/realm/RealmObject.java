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

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import io.realm.annotations.RealmClass;
import io.realm.internal.Row;

/**
 * In Realm you define your model classes by sub-classing RealmObject and adding fields to be
 * persisted. You then create your objects within a Realm, and use your custom subclasses instead
 * of using the RealmObject class directly.
 * <br>
 * An annotation processor will create a proxy class for your RealmObject subclass. The getters and
 * setters should not contain any custom code of logic as they are overridden as part of the annotation
 * process.
 * <br>
 * @see Realm#createObject(Class)
 */

@RealmClass
public abstract class RealmObject {

    protected Row row;
    protected Realm realm;

    /**
     * Removes the object from the Realm it is currently associated to.
     *
     * After this method is called the object will be invalid and any operation (read or write)
     * performed on it will fail with an IllegalStateException
     */
    public void removeFromRealm() {
        if (row == null) {
            throw new IllegalStateException("Object malformed: missing object in Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        if (realm == null) {
            throw new IllegalStateException("Object malformed: missing Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        row.getTable().moveLastOver(row.getIndex());
    }

    void populateUsingJsonObject(JSONObject json) throws JSONException {
        throw new IllegalStateException("Only use this method on objects created or fetched in a Realm, Realm.createObject() or Realm.where()");
    }

    void populateUsingJsonStream(JsonReader json) throws IOException {
        throw new IllegalStateException("Only use this method on objects created or fetched in a Realm, Realm.createObject() or Realm.where()");
    }

    /**
     * Check if the RealmObject is still valid to use ie. the RealmObject hasn't been deleted nor
     * has the {@link io.realm.Realm} been closed. It will always return false for stand alone
     * objects.
     *
     * @return {@code true} if the object is still accessible, {@code false} otherwise or if it is a
     * standalone object.
     */
    public boolean isValid() {
        return row != null && row.isAttached();
    }
}
