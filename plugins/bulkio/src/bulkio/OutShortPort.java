/*
 * This file is protected by Copyright. Please refer to the COPYRIGHT file
 * distributed with this source distribution.
 *
 * This file is part of REDHAWK bulkioInterfaces.
 *
 * REDHAWK bulkioInterfaces is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * REDHAWK bulkioInterfaces is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
/*
 * WARNING: This file is generated from OutPort.java.template.
 *          Do not modify directly.
 */
package bulkio;

import org.apache.log4j.Logger;

import BULKIO.PrecisionUTCTime;
import BULKIO.dataShortOperations;

/**
 * BulkIO output port implementation for dataShort.
 */
public class OutShortPort extends ChunkingOutPort<dataShortOperations,short[]> {

    public OutShortPort(String portName) {
        this(portName, null, null);
    }

    public OutShortPort(String portName, Logger logger) {
        this(portName, logger, null);
    }

    public OutShortPort(String portName, Logger logger, ConnectionEventListener eventCB) {
        super(portName, logger, eventCB, new ShortDataHelper());
        if (this.logger != null) {
            this.logger.debug("bulkio.OutPort CTOR port: " + portName);
        }

    }

    protected dataShortOperations narrow(final org.omg.CORBA.Object obj) {
        return BULKIO.jni.dataShortHelper.narrow(obj);
    }

    protected void sendPacket(dataShortOperations port, short[] data, PrecisionUTCTime time,
                              boolean endOfStream, String streamID) {
        port.pushPacket(data, time, endOfStream, streamID);
    }

    public String getRepid() {
        return BULKIO.dataShortHelper.id();
    }
}

