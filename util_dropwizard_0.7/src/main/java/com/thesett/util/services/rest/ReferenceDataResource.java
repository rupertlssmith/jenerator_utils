package com.thesett.util.services.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import com.thesett.util.model.RefDataItem;
import com.thesett.util.services.ReferenceDataService;

import com.thesett.aima.attribute.impl.EnumeratedStringAttribute;
import com.thesett.catalogue.model.Catalogue;

@Path("/api/refdata/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(value = MediaType.APPLICATION_JSON)
public class ReferenceDataResource implements ReferenceDataService {
    private final Catalogue model;

    /**
     * Creates the reference data RESTful service implementation.
     *
     * @param model The runtime data model.
     */
    public ReferenceDataResource(Catalogue model) {
        this.model = model;
    }

    /** {@inheritDoc} */
    @GET
    public List<String> findAllTypes() {
        List<String> result = new LinkedList<>();

        Collection<EnumeratedStringAttribute.EnumeratedStringType> enumTypes = model.getAllEnumTypes();

        for (EnumeratedStringAttribute.EnumeratedStringType enumType : enumTypes) {
            result.add(enumType.getName());
        }

        return result;
    }

    /** {@inheritDoc} */
    @GET
    @Path("{refDataName}")
    @Timed
    public List<RefDataItem> findByName(@PathParam("refDataName") String refDataName) {
        Set<EnumeratedStringAttribute> typeSet =
            EnumeratedStringAttribute.getFactoryForClass(refDataName).getType().getAllPossibleValuesSet(false);
        List<RefDataItem> result = new LinkedList<>();

        for (EnumeratedStringAttribute enumeratedString : typeSet) {
            result.add(new RefDataItem(enumeratedString.getId(), enumeratedString.getStringValue()));
        }

        Collections.sort(result);

        return result;
    }
}
