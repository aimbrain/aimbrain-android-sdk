package com.aimbrain.sdk;

import android.view.View;
import android.view.ViewGroup;

import com.aimbrain.sdk.mock.ViewIdMapperMock;

import org.junit.Test;

import java.util.LinkedList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ViewIdMapperTest {

    @Test
    public void testContainesView() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        View view = mock(View.class);
        viewIdMapper.putViewId(view, "view_id");
        assertTrue(viewIdMapper.containsView(view));
    }

    @Test
    public void testContainesViewWhenViewNotAdded() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        View view = mock(View.class);
        assertFalse(viewIdMapper.containsView(view));
    }

    @Test
    public void testRemoveViewId() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        View view = mock(View.class);
        viewIdMapper.putViewId(view, "view_id");
        assertTrue(viewIdMapper.containsView(view));
        viewIdMapper.removeViewId(view);
        assertFalse(viewIdMapper.containsView(view));
    }

    @Test
    public void testGetViewId() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        View view = mock(View.class);
        viewIdMapper.putViewId(view, "view_id");
        String view_id = viewIdMapper.getViewId(view);
        assertEquals("view_id", view_id);
    }

    @Test
    public void testGetViewIdWhenViewNotAddedToIdMap() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        View view = mock(View.class);
        when(view.getId()).thenReturn(123);
        String view_id = viewIdMapper.getViewId(view);
        assertEquals("123", view_id);
    }

    @Test
    public void testExtractViewPathWhenHasParentViews() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        ViewGroup view1 = mock(ViewGroup.class);
        ViewGroup view2 = mock(ViewGroup.class);
        ViewGroup view3 = mock(ViewGroup.class);

        when(view1.getId()).thenReturn(1);
        when(view2.getId()).thenReturn(2);
        when(view3.getId()).thenReturn(3);

        when(view3.getParent()).thenReturn(view2);
        when(view2.getParent()).thenReturn(view1);
        when(view1.getParent()).thenReturn(null);

        LinkedList<String> list = viewIdMapper.extractViewPath(view3);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("3", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("1", list.get(2));

    }

    @Test
    public void testExtractViewPath() throws Exception {
        ViewIdMapperMock viewIdMapper = new ViewIdMapperMock();
        ViewGroup view1 = mock(ViewGroup.class);

        when(view1.getId()).thenReturn(1);
        when(view1.getParent()).thenReturn(null);

        LinkedList<String> list = viewIdMapper.extractViewPath(view1);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0));

    }
}
