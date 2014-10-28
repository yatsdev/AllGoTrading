package org.yats.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UniqueIdTest {


        @Test(groups = { "inMemory" })
        public void canCompare()
        {
            UniqueId id2 = UniqueId.create();
            assert (!id1.isSameAs(id2));
            assert(id1.isSameAs(id1));
        }

        @Test(groups = { "inMemory" })
        public void canCopy()
        {
            UniqueId id1Copy = UniqueId.createFromString(id1.toString());
            assert (id1.isSameAs(id1Copy));
        }


        @BeforeMethod(groups = { "inMemory" })
        public void setUp() {
            id1 = UniqueId.create();
        }

    UniqueId id1;

} // class
