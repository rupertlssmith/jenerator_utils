package com.thesett.test.controllers;

/**
 * ReflectiveServiceFactory provides a way of obtaining services, which may also have been wrapped with test
 * functionality, for the purpose of writing tests against them.
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th>
 * <tr><td> Provide a service reflectively by its interface. </td></tr>
 * </table></pre>
 */
public interface ReflectiveServiceFactory {
    /**
     * Provides a Service reflectively by its interface.
     *
     * @param  serviceClass The class of the service interface to supply.
     * @param  <S>          The type of the service.
     *
     * @return A service implementation for the interface specified.
     */
    <S> S getService(Class<? extends S> serviceClass);
}
