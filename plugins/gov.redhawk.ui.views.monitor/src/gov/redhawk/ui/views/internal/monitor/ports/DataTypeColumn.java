/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package gov.redhawk.ui.views.internal.monitor.ports;

import CF.DataType;
import gov.redhawk.ui.views.internal.monitor.labels.DataTypeCellLabelProvider;

public class DataTypeColumn extends Column {

	public DataTypeColumn(final DataType type) {
		super(type.id, type.id, new DataTypeCellLabelProvider(type.id));
	}
}
