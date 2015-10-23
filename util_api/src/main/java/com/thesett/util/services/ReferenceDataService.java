package com.thesett.util.services;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.thesett.util.model.RefDataItem;

/**
 * Reference Data Service returns all of the reference data stored in the database.
 */
@Path("/api/refdata/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public interface ReferenceDataService {
    /**
     * Lists all reference data types in the database by type name.
     *
     * @return All reference data types in the database by type name.
     */
    @GET
    List<String> findAllTypes();

    /**
     * Lists all reference data in the database.
     *
     * @param  refDataName - the name of the reference data you wish to retrieve
     *
     * @return All reference data in the database.
     */
    /** {@inheritDoc} */
    @GET
    @Path("{refDataName}")
    List<RefDataItem> findByName(@PathParam("refDataName") String refDataName);
}
