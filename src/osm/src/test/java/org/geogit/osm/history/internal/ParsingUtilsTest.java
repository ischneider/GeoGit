/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the LGPL 2.1 license, available at the root
 * application directory.
 */

package org.geogit.osm.history.internal;

import static org.geogit.osm.history.internal.ParsingUtils.parseDateTime;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ParsingUtilsTest extends Assert {

    @Test
    public void test() {
        long dateTime = parseDateTime("2009-10-11T20:02:09Z");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(dateTime);
        assertEquals(2009, cal.get(Calendar.YEAR));
        assertEquals(9, cal.get(Calendar.MONTH));
        assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(20, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(2, cal.get(Calendar.MINUTE));
        assertEquals(9, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

}
