/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.proxy.mobileterminal;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.mock.MockPortType;

/**
 **/
/**
 * This class is intended to initiate the PortType for the intended WS-calls
 **/
@Singleton
@Startup
public class PortInitiator {

    //Replace with one or several portTypes
    private MockPortType vesselPort;

    public MockPortType getPort() {
        if (vesselPort == null) {
            vesselPort = setupPort();
        }
        return vesselPort;
    }

    private static Logger LOG = LoggerFactory.getLogger(PortInitiator.class);

    /**
     * Example of setting up a WS endpoint port
     *
     * @return
     */
    private MockPortType setupPort() {
        LOG.info("Initiating port.");

        /*final VesselService service = new VesselService();
         VesselPortType port = service.getVesselPortType();
         BindingProvider bp = (BindingProvider) port;
         Map<String, Object> context = bp.getRequestContext();
         context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, Constants.VESSEL_ENDPOINT);*/
        MockPortType port = new MockPortType();
        return port;
    }

}