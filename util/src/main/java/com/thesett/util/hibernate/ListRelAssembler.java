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

import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;

/**
 * ListRelAssembler joins together 2 entities that have a parent/child relationship represented as a list on the parent.
 * It expects to receive the 2 entities and the list index in the array of objects, with the child at position 0, the
 * list index at position 1 and the parent at position 2.
 *
 * <p/>This is for use in situations where Hibernate is not to create the relationship query itself, but it is available
 * in the output of some other query. For example, a native query to fetch a recursive relationship.
 *
 * <p/>The parent and child entities are evicted from the session, so that the work on the list does not cause Hibernate
 * to fetch the list. The list should already be known from the query. As the entities are evicted, this is for reading
 * parent/child relationships only - not updating them.
 *
 * <p/>In order to work with parent and child of any type, abstract methods {@link #newList(Object)} and
 * {@link #getList(Object)} are to be implemented in a type specific way to create a new list on the parent, and to
 * supply the list on the parent to add new elements to.
 *
 * <p/>The list elements must be presented in order, from index 0 upwards. An 'ORDER BY' clause may be needed on the
 * query to achieve this.
 */
public abstract class ListRelAssembler<E> implements ResultTransformer
{
    /** The Hibernate session for evicting parent and child. */
    private final Session session;

    /**
     * Creates the ListRelAssembler with the current session.
     *
     * @param session The current Hibernate session (for evicting the parent and child).
     */
    public ListRelAssembler(Session session)
    {
        this.session = session;
    }

    /** {@inheritDoc} */
    public Object transformTuple(Object[] objects, String[] strings)
    {
        Object parent = objects[2];
        Object child = objects[0];

        if (child!=null) {
            int index = (Integer) objects[1];

            session.evict(parent);
            session.evict(child);

            if (index == 0) {
                newList((E) parent);
            }

            getList((E) parent).add(child);
        }

        return parent;
    }

    /** {@inheritDoc} */
    public List transformList(List list)
    {
        return list;
    }

    /**
     * Creates a new empty list on the parent.
     *
     * @param parent The parent to create the new empty list on.
     */
    protected abstract void newList(E parent);

    /**
     * Provides the list on the parent, that child elements are to be added to.
     *
     * @param  parent The parent to get the child list from.
     *
     * @return The list to place child elements into.
     */
    protected abstract List getList(E parent);
}
