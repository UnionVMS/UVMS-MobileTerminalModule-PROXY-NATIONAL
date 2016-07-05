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
package eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event;

import javax.jms.TextMessage;

import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalFault;

public class MobileTerminalProxyEventMessage {

    private TextMessage requestMessage;
    private String responseMessage;
    private MobileTerminalFault errorMessage;

    public MobileTerminalProxyEventMessage(TextMessage requestMessage, MobileTerminalFault errorMessage) {
        this.requestMessage = requestMessage;
        this.errorMessage = errorMessage;
    }

    public MobileTerminalProxyEventMessage(TextMessage requestMessage, String responseMessage) {
        this.requestMessage = requestMessage;
        this.responseMessage = responseMessage;
    }

    public TextMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(TextMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public MobileTerminalFault getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(MobileTerminalFault errorMessage) {
        this.errorMessage = errorMessage;
    }

}