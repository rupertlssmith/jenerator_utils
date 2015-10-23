package com.thesett.util.json;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

/**
 * JsonClientProxy implements a dynamic proxy, that performs serialisation/deserialisation on all arguments and return
 * values around a method call. All object values are serialised through JSON, and in this way it simulates a webservice
 * call using JSON.
 *
 * <p/>Here is an example of using this proxy to force serialisation of all objects through JSON:
 *
 * <pre>
 * ManageApplicationsPort port = new ManageApplications().getManageApplicationsPort();
 *
 * SomeService foo = (SomeService) Proxy.newProxyInstance(port.getClass().getClassLoader(),
 * new Class[] { ManageApplicationsService.class }, new JsonClientProxy(port));
 * </pre>
 *
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities <th> Collaborations
 * <tr><td> Force serialisation through JSON. </td></tr>
 * </table></pre>
 *
 * @author Rupert Smith
 */
public class JsonClientProxy implements InvocationHandler {
    /** Used to convert to JSON and back. */
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    /** The object being proxied. */
    private final Object obj;

    /**
     * Creates an instance of the standalone webservice test proxy.
     *
     * @param obj The object being proxied.
     */
    public JsonClientProxy(Object obj) {
        this.obj = obj;
    }

    /** {@inheritDoc} */
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Object[] serdesArgs = new Object[args.length];

        // Push the arguments through JSON.
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            if (arg == null) {
                serdesArgs[i] = null;
            } else {
                serdesArgs[i] = MAPPER.readValue(MAPPER.writeValueAsString(arg), arg.getClass());
            }
        }

        Object result;

        try {
            result = m.invoke(obj, serdesArgs);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }

        Object serdesResult;

        // Push the return value through JSON.
        if (result == null) {
            serdesResult = null;
        } else {
            serdesResult = MAPPER.readValue(MAPPER.writeValueAsString(result), result.getClass());
        }

        return serdesResult;
    }
}
