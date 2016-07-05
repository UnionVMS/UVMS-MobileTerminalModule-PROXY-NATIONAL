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
package eu.europa.ec.fisheries.uvms.proxy.mobileterminal.message;


import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.mobileterminal.source.v1.GetMobileTerminalRequest;
import eu.europa.ec.fisheries.schema.mobileterminal.source.v1.MobileTerminalBaseRequest;
import eu.europa.ec.fisheries.schema.mobileterminal.source.v1.MobileTerminalDataSourceMethod;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.MobileTerminalDataSourceResponseMapper;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.MobileTerminalClientProxy;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.constant.Constants;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxyErrorEvent;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxyEventMessage;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxySendEvent;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.exception.MobileTerminalProxyException;

/**
 **/
@MessageDriven(mappedName = Constants.NATIONAL_PROXY_QUEUE, activationConfig = {
    @ActivationConfigProperty(propertyName = "messagingType", propertyValue = Constants.CONNECTION_TYPE),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = Constants.DESTINATION_TYPE_QUEUE),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = Constants.NATIONAL_PROXY_QUEUE_NAME),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class ProxyMessageReciever implements MessageListener {

    @EJB
    MobileTerminalClientProxy client;

    @Inject
    @MobileTerminalProxySendEvent
    Event<MobileTerminalProxyEventMessage> sendBackEvent;

    @Inject
    @MobileTerminalProxyErrorEvent
    Event<MobileTerminalProxyEventMessage> errorEvent;

    private static Logger LOG = LoggerFactory.getLogger(ProxyMessageReciever.class);

    @Override
    public void onMessage(Message message) {
        LOG.info("Message received in MobileTerminal national proxy service.");
        TextMessage requestMessage = (TextMessage) message;

        try {

            MobileTerminalBaseRequest request = JAXBMarshaller.unmarshallTextMessage(requestMessage, MobileTerminalBaseRequest.class);
            MobileTerminalDataSourceMethod method = request.getMethod();

            String returnMessage = null;

            switch (method) {
                case GET:
                    GetMobileTerminalRequest getRequest = JAXBMarshaller.unmarshallTextMessage(requestMessage, GetMobileTerminalRequest.class);
                    MobileTerminalType terminalGet = client.getMobileTerminalById(getRequest.getId());
                    returnMessage = MobileTerminalDataSourceResponseMapper.createMobileTerminalResponse(terminalGet);
                    break;
                case UPDATE:
                case CREATE:
                case LIST:
                case ASSIGN:
                case UNASSIGN:
                case TERMINAL_SYSTEM_LIST:
                case DELETE:
                default:
                    LOG.debug("Not implemented method consumed");
                    errorEvent.fire(new MobileTerminalProxyEventMessage(requestMessage, MobileTerminalDataSourceResponseMapper.createFaultMessage("Method not implemented")));
                    break;
            }

            sendBackEvent.fire(new MobileTerminalProxyEventMessage(requestMessage, returnMessage));

        } catch (MobileTerminalModelException | MobileTerminalProxyException e) {
            LOG.error("[ Error when consuming message. ] {} {}", e.getMessage(), e.getStackTrace());
            errorEvent.fire(new MobileTerminalProxyEventMessage(requestMessage, MobileTerminalDataSourceResponseMapper.createFaultMessage(e.getMessage())));
        }

    }

}