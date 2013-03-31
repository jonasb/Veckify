package com.wigwamlabs.spotify.tests;

import com.wigwamlabs.spotify.DummyNativeItemContainer;
import junit.framework.TestCase;

public class NativeItemContainerTest extends TestCase {
    public void testAddingMultipleAtStart() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("new-1 new-2 new-3 a b c".split("\\s+"));
        container.onItemsMoved(new int[]{-1, -1, -1}, 0);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testAddingMultipleAtEnd() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("a b c new-1 new-2 new-3".split("\\s+"));
        container.onItemsMoved(new int[]{-1, -1, -1}, 3);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testRemoveMultipleAtStart() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("d e f".split("\\s+"));
        container.onItemsMoved(new int[]{0, 1, 2}, -1);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testRemoveMultipleAtEnd() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("a b c".split("\\s+"));
        container.onItemsMoved(new int[]{3, 4, 5}, -1);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testRemoveRandomOrder() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f g".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("b d f".split("\\s+"));
        container.onItemsMoved(new int[]{6, 0, 4, 2}, -1);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveBlockToStart() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("d e f a b c".split("\\s+"));
        container.onItemsMoved(new int[]{3, 4, 5}, 0);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveBlockToEnd() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("d e f a b c".split("\\s+"));
        container.onItemsMoved(new int[]{0, 1, 2}, 6);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandomToStart() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f g".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("g a e c  b d f".split("\\s+"));
        container.onItemsMoved(new int[]{6, 0, 4, 2}, 0);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandomToEnd() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("a b c d e f g".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("b d f  g a e c".split("\\s+"));
        container.onItemsMoved(new int[]{6, 0, 4, 2}, 7);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandom2to0() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("0 1 2 3".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("1 3 0 2".split("\\s+"));
        container.onItemsMoved(new int[]{1, 3}, 0);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandom2to1() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("0 1 2 3".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("0 1 3 2".split("\\s+"));
        container.onItemsMoved(new int[]{1, 3}, 1);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandom2to2() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("0 1 2 3".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("0 1 3 2".split("\\s+"));
        container.onItemsMoved(new int[]{1, 3}, 2);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandom2to3() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("0 1 2 3".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("0 2 1 3".split("\\s+"));
        container.onItemsMoved(new int[]{1, 3}, 3);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

    public void testMoveRandom2to4() {
        final DummyNativeItemContainer container = new DummyNativeItemContainer();
        container.setNativeItems("0 1 2 3".split("\\s+"));
        assertEquals(container.getNativeItems(), container.getContainerItems());

        container.setNativeItems("0 2 1 3".split("\\s+"));
        container.onItemsMoved(new int[]{1, 3}, 4);
        assertEquals(container.getNativeItems(), container.getContainerItems());
    }

}
