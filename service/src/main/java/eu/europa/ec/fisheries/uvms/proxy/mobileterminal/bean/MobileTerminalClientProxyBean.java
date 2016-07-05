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
package eu.europa.ec.fisheries.uvms.proxy.mobileterminal.bean;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.CarrierId;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.CarrierIdType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.CarrierType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalAttribute;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalFieldType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalId;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalIdType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalIdTypeType;
import eu.europa.ec.fisheries.schema.mobileterminal.types.v1.MobileTerminalType;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelMapperException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.exception.MobileTerminalModelValidationException;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mapper.MobileTerminalGenericMapper;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.mock.MockData;
import eu.europa.ec.fisheries.uvms.mobileterminal.model.validator.MobileTerminalDataSourceRequestValidator;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.MobileTerminalClientProxy;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.PortInitiator;
import eu.europa.ec.fisheries.uvms.proxy.mobileterminal.exception.MobileTerminalProxyException;

/**
 **/
@Stateless
public class MobileTerminalClientProxyBean implements MobileTerminalClientProxy {

    @EJB
    PortInitiator port;

    private static Logger LOG = LoggerFactory.getLogger(MobileTerminalClientProxyBean.class);

    @Override
    public MobileTerminalType getMobileTerminalById(MobileTerminalId id) throws MobileTerminalProxyException {

        try {

            MobileTerminalDataSourceRequestValidator.validateMobileTerminalId(id);

            String internalId = MobileTerminalGenericMapper.getMobileTerminalIdTypeValue(id, MobileTerminalIdTypeType.INTERNAL_ID);
            String serialNumber = MobileTerminalGenericMapper.getMobileTerminalIdTypeValue(id, MobileTerminalIdTypeType.SERIAL_NUMBER);

            MobileTerminalType response = MockData.createMobileTerminalDtoWithoutIds(Integer.parseInt(internalId), id.getSystemType());

            MobileTerminalIdType serialNumberResponse = MockData.createMobileTerminalIdType(MobileTerminalIdTypeType.SERIAL_NUMBER, id.toString());
            response.getMobileTerminalId().getIdList().add(serialNumberResponse);

            MobileTerminalAttribute antenna = MockData.createMobileTerminalAttribute(MobileTerminalFieldType.ANTENNA, "ANTENNA from Proxy Service");
            MobileTerminalAttribute answerBack = MockData.createMobileTerminalAttribute(MobileTerminalFieldType.INSTALLED_BY, "INSTALLED_BY from Proxy Service");
            MobileTerminalAttribute installedOn = MockData.createMobileTerminalAttribute(MobileTerminalFieldType.INSTALLED_ON, "INSTALLED_ON from Proxy Service");
            MobileTerminalAttribute softWareVersion = MockData.createMobileTerminalAttribute(MobileTerminalFieldType.SOFTWARE_VERSION, "SOFTWARE_VERSION from Proxy Service");

            List<MobileTerminalAttribute> attributes = new ArrayList<>();

            response.getAttributes().add(antenna);
            response.getAttributes().add(answerBack);
            response.getAttributes().add(installedOn);
            response.getAttributes().add(softWareVersion);

            CarrierId carrier = MockData.createCarrierId(CarrierType.VESSEL, CarrierIdType.IRCS, "SLAK");
            response.setCarrierId(carrier);
            return response;

        } catch (MobileTerminalModelValidationException | MobileTerminalModelMapperException e) {
            LOG.error("[ Error when getting mobile terminal by ID. ] {}", e.getMessage());
            throw new MobileTerminalProxyException(e.getMessage());
        }

    }

}