package com.thesett.util.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <pre><p/><table id="crc"><caption>CRC Card</caption>
 * <tr><th> Responsibilities </th><th> Collaborations </th>
 * <tr><td>  </td></tr>
 * </table></pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefDataItem implements Comparable<RefDataItem> {
    /** Holds the database surrogate id. */
    private Long id;

    /** Holds the name property. */
    protected String name;

    /** No-arg constructor for serialization. */
    public RefDataItem() {
    }

    /** The full constructor to build the component from all of its elements. */
    public RefDataItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * {@inheritDoc}
     *
     * <p/>Provides an ordering on ref data items by id ascending.
     */
    public int compareTo(RefDataItem o) {
        return (id < o.id) ? -1 : (id > o.id) ? 1 : 0;
    }

    /**
     * Gets the database surrogate id.
     *
     * @return The database surrogate id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database surrogate id.
     *
     * @param id The database surrogate id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Accepts a new value for the name property.
     *
     * @param name The name property.
     */
    public RefDataItem withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Provides the name property.
     *
     * @return The name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Accepts a new value for the name property.
     *
     * @param name The name property.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines whether an object of this type is equal to another object. To be equal the object being compared to
     * (the comparator) must be an instance of this class and have identical natural key field values to this one.
     *
     * @param  o The object to compare to.
     *
     * @return True if the comparator is equal to this, false otherwise.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof RefDataItem) {
            RefDataItem comp = (RefDataItem) o;

            return id == null ? comp.id == null : id.equals(comp.id);
        } else {
            return false;
        }
    }

    /**
     * Computes a hash code for the component that conforms with its equality method, being based on the same set of
     * fields that are used to compute equality.
     *
     * @return A hash code of the components equality value.
     */
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    /** Pretty printing for debugging purposes. */
    public String toString() {
        return "RefDataItem: [ id = " + id + ", name = " + name + " ]";
    }
}
