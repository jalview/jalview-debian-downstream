/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package uk.ac.ebi.www.picr.AccessionMappingService;

import jalview.util.MessageManager;

public class AccessionMapperServiceLocator extends
        org.apache.axis.client.Service implements
        uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperService
{

  public AccessionMapperServiceLocator()
  {
  }

  public AccessionMapperServiceLocator(
          org.apache.axis.EngineConfiguration config)
  {
    super(config);
  }

  // Use to get a proxy class for AccessionMapperPort
  private java.lang.String AccessionMapperPort_address = "http://www.ebi.ac.uk:80/Tools/picr/service";

  public java.lang.String getAccessionMapperPortAddress()
  {
    return AccessionMapperPort_address;
  }

  // The WSDD service name defaults to the port name.
  private java.lang.String AccessionMapperPortWSDDServiceName = "AccessionMapperPort";

  public java.lang.String getAccessionMapperPortWSDDServiceName()
  {
    return AccessionMapperPortWSDDServiceName;
  }

  public void setAccessionMapperPortWSDDServiceName(java.lang.String name)
  {
    AccessionMapperPortWSDDServiceName = name;
  }

  public uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperInterface getAccessionMapperPort()
          throws javax.xml.rpc.ServiceException
  {
    java.net.URL endpoint;
    try
    {
      endpoint = new java.net.URL(AccessionMapperPort_address);
    } catch (java.net.MalformedURLException e)
    {
      throw new javax.xml.rpc.ServiceException(e);
    }
    return getAccessionMapperPort(endpoint);
  }

  public uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperInterface getAccessionMapperPort(
          java.net.URL portAddress) throws javax.xml.rpc.ServiceException
  {
    try
    {
      uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperBindingStub _stub = new uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperBindingStub(
              portAddress, this);
      _stub.setPortName(getAccessionMapperPortWSDDServiceName());
      return _stub;
    } catch (org.apache.axis.AxisFault e)
    {
      return null;
    }
  }

  public void setAccessionMapperPortEndpointAddress(java.lang.String address)
  {
    AccessionMapperPort_address = address;
  }

  /**
   * For the given interface, get the stub implementation. If this service has
   * no port for the given interface, then ServiceException is thrown.
   */
  public java.rmi.Remote getPort(Class serviceEndpointInterface)
          throws javax.xml.rpc.ServiceException
  {
    try
    {
      if (uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperInterface.class
              .isAssignableFrom(serviceEndpointInterface))
      {
        uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperBindingStub _stub = new uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperBindingStub(
                new java.net.URL(AccessionMapperPort_address), this);
        _stub.setPortName(getAccessionMapperPortWSDDServiceName());
        return _stub;
      }
    } catch (java.lang.Throwable t)
    {
      throw new javax.xml.rpc.ServiceException(t);
    }
    throw new javax.xml.rpc.ServiceException(MessageManager.formatMessage(
            "exception.no_stub_implementation_for_interface",
            new String[] { (serviceEndpointInterface == null ? "null"
                    : serviceEndpointInterface.getName()) }));
  }

  /**
   * For the given interface, get the stub implementation. If this service has
   * no port for the given interface, then ServiceException is thrown.
   */
  public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
          Class serviceEndpointInterface)
          throws javax.xml.rpc.ServiceException
  {
    if (portName == null)
    {
      return getPort(serviceEndpointInterface);
    }
    java.lang.String inputPortName = portName.getLocalPart();
    if ("AccessionMapperPort".equals(inputPortName))
    {
      return getAccessionMapperPort();
    }
    else
    {
      java.rmi.Remote _stub = getPort(serviceEndpointInterface);
      ((org.apache.axis.client.Stub) _stub).setPortName(portName);
      return _stub;
    }
  }

  public javax.xml.namespace.QName getServiceName()
  {
    return new javax.xml.namespace.QName(
            "http://www.ebi.ac.uk/picr/AccessionMappingService",
            "AccessionMapperService");
  }

  private java.util.HashSet ports = null;

  public java.util.Iterator getPorts()
  {
    if (ports == null)
    {
      ports = new java.util.HashSet();
      ports.add(new javax.xml.namespace.QName(
              "http://www.ebi.ac.uk/picr/AccessionMappingService",
              "AccessionMapperPort"));
    }
    return ports.iterator();
  }

  /**
   * Set the endpoint address for the specified port name.
   */
  public void setEndpointAddress(java.lang.String portName,
          java.lang.String address) throws javax.xml.rpc.ServiceException
  {
    if ("AccessionMapperPort".equals(portName))
    {
      setAccessionMapperPortEndpointAddress(address);
    }
    else
    { // Unknown Port Name
      throw new javax.xml.rpc.ServiceException(
              MessageManager.formatMessage(
                      "exception.cannot_set_endpoint_address_unknown_port",
                      new String[] { portName }));
    }
  }

  /**
   * Set the endpoint address for the specified port name.
   */
  public void setEndpointAddress(javax.xml.namespace.QName portName,
          java.lang.String address) throws javax.xml.rpc.ServiceException
  {
    setEndpointAddress(portName.getLocalPart(), address);
  }

}
