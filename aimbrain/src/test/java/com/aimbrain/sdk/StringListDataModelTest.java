package com.aimbrain.sdk;

import com.aimbrain.sdk.models.StringListDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class StringListDataModelTest {

    @Test
    public void testToJSON() throws JSONException {
        StringListDataModel model = new StringListDataModel();
        List<String> data = mock(List.class);
        Iterator iterator = mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true, true, false);
        when(iterator.next()).thenReturn("").thenReturn("");

        when(data.iterator()).thenReturn(iterator);
        model.setData(data);
        JSONArray jsonArray = model.toJSON();
        assertNotNull(jsonArray);
        assertEquals(2, jsonArray.length());
    }
}
