/* Copyright Rupert Smith, 2005 to 2008, all rights reserved. */
/*
 * Copyright The Sett Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thesett.util.hibernate;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.transform.ResultTransformer;

/**
 * SetRelAssembler joins together 2 entities that have a subject/object relationship represented as a set on the
 * subject. It expects to receive the 2 entities in the array of objects, with the object at position 0, and the subject
 * at position 2.
 *
 * <p/>This is for use in situations where Hibernate is not to create the relationship query itself, but it is available
 * in the output of some other query. For example, a native query to fetch a recursive relationship.
 *
 * <p/>The subject and object entities are evicted from the session, so that the work on the set does not cause
 * Hibernate to fetch the set. The set should already be known from the query. As the entities are evicted, this is for
 * reading relationships only - not updating them.
 *
 * <p/>In order to work with entities of any type, abstract methods {@link #newSet(Object)} and {@link #getSet(Object)}
 * are to be implemented in a type specific way to create a new set on the subject, and to supply the set on the subject
 * to add new elements to.
 */
public abstract class SetRelAssembler<E> implements ResultTransformer
{
    /** The Hibernate session for evicting parent and child. */
    private final Session session;

    /**
     * Creates the ListRelAssembler with the current session.
     *
     * @param session The current Hibernate session (for evicting the parent and child).
     */
    public SetRelAssembler(Session session)
    {
        this.session = session;
    }

    /** {@inheritDoc} */
    public Object transformTuple(Object[] objects, String[] strings)
    {
        Object subject = objects[1];
        Object object = objects[0];

        if (object != null)
        {
            session.evict(subject);
            session.evict(object);

            if (getSet((E) subject) instanceof PersistentSet)
            {
                newSet((E) subject);
            }

            getSet((E) subject).add(object);
        }

        return subject;
    }

    /** {@inheritDoc} */
    public List transformList(List list)
    {
        return list;
    }

    /**
     * Creates a new empty set on the subject.
     *
     * @param subject The subject to create the new empty set on.
     */
    protected abstract void newSet(E subject);

    /**
     * Provides the list on the subject, that object elements are to be added to.
     *
     * @param  subject The subject to get the object set from.
     *
     * @return The list to place object elements into.
     */
    protected abstract Set getSet(E subject);
}
