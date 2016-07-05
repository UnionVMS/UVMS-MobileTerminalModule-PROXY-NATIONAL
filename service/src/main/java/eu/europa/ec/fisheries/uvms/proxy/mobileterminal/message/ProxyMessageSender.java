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

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelMapperException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.constant.Constants;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxyErrorEvent;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxyEventMessage;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxySendEvent;

/**
 **/
@LocalBean
@Stateless
public class ProxyMessageSender {

    @Resource(lookup = Constants.CONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;

    private Connection connection = null;
    private Session session = null;

    @Inject
    @MobileTerminalProxyErrorEvent
    Event<MobileTerminalProxyEventMessage> errorEvent;

    private static Logger LOG = LoggerFactory.getLogger(ProxyMessageSender.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void returnMobileTerminal(@Observes @MobileTerminalProxySendEvent MobileTerminalProxyEventMessage message) {
        try {
            LOG.info("Sending message back to recipient from MobileTerminal Proxy service with correlationId: {} on queue: {}.", message.getRequestMessage().getJMSMessageID(), message.getRequestMessage().getJMSReplyTo());

            connectToQueue();

            TextMessage responseMessage = session.createTextMessage(message.getResponseMessage());
            responseMessage.setJMSCorrelationID(message.getRequestMessage().getJMSMessageID());
            session.createProducer(message.getRequestMessage().getJMSReplyTo()).send(responseMessage);
        } catch (JMSException e) {
            LOG.error("[ Error when sending response back to recipient. ] {} {}", e.getMessage(), e.getStackTrace());
            errorEvent.fire(new MobileTerminalProxyEventMessage(message.getRequestMessage(), "Exception when sending response back to recipient : " + e.getMessage()));
        } finally {
            try {
                connection.stop();
                connection.close();
            } catch (JMSException e) {
                LOG.error("[ Error when stopping or closing JMS queue. ] {} {}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void returnError(@Observes @MobileTerminalProxyErrorEvent MobileTerminalProxyEventMessage message) {
        try {
            LOG.info("Sending error message back from MobileTerminal Proxy service to recipient om JMS Queue with correlationID: {}.", message.getRequestMessage().getJMSMessageID());
            
            connectToQueue();

            String data = JAXBMarshaller.marshallJaxBObjectToString(message.getErrorMessage());
            TextMessage response = session.createTextMessage(data);
            response.setJMSCorrelationID(message.getRequestMessage().getJMSMessageID());
            session.createProducer(message.getRequestMessage().getJMSReplyTo()).send(response);
        } catch (JMSException | MobileTerminalModelMapperException e) {
            LOG.error("[ Error when returning Error message to recipient. ] {} {}", e.getMessage(), e.getStackTrace());
        } finally {
            try {
                connection.stop();
                connection.close();
            } catch (JMSException e) {
                LOG.error(" [ Error when stopping or closing JMS queue. ] {} {}", e.getMessage(), e.getStackTrace());
            }
        }

    }

    private void connectToQueue() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }

}