/*
 * This file is protected by Copyright. Please refer to the COPYRIGHT file 
 * distributed with this source distribution.
 * 
 * This file is part of REDHAWK core.
 * 
 * REDHAWK core is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * REDHAWK core is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */

/*
 * WARNING: This file is generated from ComplexProperty.template.
 *          Do not modify directly.
 */

package org.ossie.properties;

import org.omg.CORBA.Any;

import CF.complexBoolean;
import CF.complexBooleanHelper;

public class ComplexBooleanProperty extends AbstractSimpleProperty<complexBoolean> {
    public ComplexBooleanProperty(String id, String name, complexBoolean value, Mode mode, Action action, Kind[] kinds) {
        super(id, name, "boolean", value, mode, action, kinds);
    }

    public ComplexBooleanProperty(String id, String name, complexBoolean value, Mode mode, Action action, Kind[] kinds, boolean optional) {
        super(id, name, "boolean", value, mode, action, kinds, optional);
    }

    protected complexBoolean extract(Any any) {
        try {
            return (complexBoolean)AnyUtils.convertAny(any);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Incorrect any type recevied");
        }
    }

    protected void insert(Any any, complexBoolean value) {
        complexBooleanHelper.insert(any, value);
    }

    protected complexBoolean parseString(String str) {
        return ComplexUtils.parseComplexBoolean(str);
    }
}
