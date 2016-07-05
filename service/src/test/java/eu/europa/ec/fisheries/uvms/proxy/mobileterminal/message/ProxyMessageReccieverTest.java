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

import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalId;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalIdType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalIdTypeType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalSource;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.TerminalSystemType;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelMapperException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelValidationException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.MobileTerminalDataSourceRequestMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mock.MockData;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.MobileTerminalClientProxy;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxyErrorEvent;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxyEventMessage;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.event.MobileTerminalProxySendEvent;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.exception.MobileTerminalProxyException;
import javax.enterprise.event.Event;
import javax.jms.TextMessage;
import static org.mockito.Matchers.any;
import org.mockito.MockitoAnnotations;

/**
 **/
@RunWith(MockitoJUnitRunner.class)
public class ProxyMessageReccieverTest {

    private static final Integer ID = 1;
    private static final String STRING = "String";

    @Mock
    MobileTerminalClientProxy client;

    @Mock
    JMSContext inMessageContext;

    @InjectMocks
    ProxyMessageReciever receiver;

    @Mock
    TextMessage message;

    @Mock
    TextMessage replyMessage;

    @Mock
    Queue queue;

    @Mock
    JMSProducer producer;

    @Mock
    @MobileTerminalProxySendEvent
    Event<MobileTerminalProxyEventMessage> sendBackEvent;

    @Mock
    @MobileTerminalProxyErrorEvent
    Event<MobileTerminalProxyEventMessage> errorEvent;

    MobileTerminalType RESPONSE_DTO = MockData.createMobileTerminalDto(ID);
    MobileTerminalId REQUEST_DTO = MockData.createMobileTerminalId(ID, TerminalSystemType.INMARSAT_C);
    String REQUEST_MESSAGE_STRING;

    @Before
    public void setUp() throws JMSException, MobileTerminalModelMapperException {
        MockitoAnnotations.initMocks(this);

        MobileTerminalIdType serialNumber = MockData.createMobileTerminalIdType(MobileTerminalIdTypeType.SERIAL_NUMBER, ID.toString());
        MobileTerminalIdType internalId = MockData.createMobileTerminalIdType(MobileTerminalIdTypeType.INTERNAL_ID, ID.toString());
        REQUEST_DTO.getIdList().add(internalId);
        REQUEST_DTO.getIdList().add(serialNumber);

        REQUEST_MESSAGE_STRING = MobileTerminalDataSourceRequestMapper.mapGetMobileTerminal(REQUEST_DTO);

        when(inMessageContext.createTextMessage()).thenReturn(message);
        when(message.getJMSReplyTo()).thenReturn(queue);
        when(message.getText()).thenReturn(REQUEST_MESSAGE_STRING);
        when(inMessageContext.createProducer()).thenReturn(producer);
    }

    @Test
    public void testGetById() throws JMSException {
        try {
            when(client.getMobileTerminalById(REQUEST_DTO)).thenReturn(RESPONSE_DTO);

            receiver.onMessage(message);

            Mockito.verify(client).getMobileTerminalById(REQUEST_DTO);
            Mockito.verify(sendBackEvent).fire(any(MobileTerminalProxyEventMessage.class));

        } catch (MobileTerminalProxyException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test()
    public void testNotImplementedYet() throws JMSException, MobileTerminalModelMapperException, MobileTerminalModelValidationException {
        try {
            
            MobileTerminalType type = MockData.createMobileTerminalDto(ID);
            String NOT_IMPLEMENTED_MESSAGE_STRING = MobileTerminalDataSourceRequestMapper.mapGetCreateMobileTerminal(type, MobileTerminalSource.NATIONAL);
            
            when(message.getText()).thenReturn(NOT_IMPLEMENTED_MESSAGE_STRING);
            when(client.getMobileTerminalById(REQUEST_DTO)).thenReturn(RESPONSE_DTO);

            receiver.onMessage(message);

            Mockito.verify(errorEvent).fire(any(MobileTerminalProxyEventMessage.class));

        } catch (MobileTerminalProxyException ex) {
            Assert.fail(ex.getMessage());
        }
    }

}