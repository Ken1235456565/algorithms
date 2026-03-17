package com.phasmidsoftware.dsaipg.adt.pq;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Fibonacci Heap implementation supporting give() and take() operations.
 * Amortized O(1) for give(), O(log n) for take().
 *
 * @param <K> the type of elements held in this heap.
 */
public class FibonacciHeap<K> {
    private static class Node<K> {
        K key;
        int degree = 0;
        boolean marked = false;
        Node<K> parent = null;
        Node<K> child  = null;
        Node<K> left;
        Node<K> right;

        Node(K key) {
            this.key  = key;
            this.left = this;
            this.right = this;
        }
    }

    private final Comparator<K> comparator;
    private final boolean       max;
    private final int           capacity;
    private Node<K>             top;          // 当前最优根节点
    private int                 size;
    private K                   highestSpilled = null;

    public FibonacciHeap(int capacity, boolean max, Comparator<K> comparator) {
        this.capacity   = capacity;
        this.max        = max;
        this.comparator = comparator;
    }



    /**
     * Insert a key. If the heap is at capacity, track the highest-priority spilled element.
     */
    public void give(K key) {
        if (size == capacity) {
            // 堆满：记录溢出元素中优先级最高者
            if (highestSpilled == null || better(key, highestSpilled))
                highestSpilled = key;
            return;
        }
        Node<K> node = new Node<>(key);
        top = mergeLists(top, node);
        if (top == null || better(node.key, top.key)) top = node;
        size++;
    }

    /**
     * Remove and return the highest-priority element.
     */
    public K take() {
        if (top == null) throw new NoSuchElementException("FibonacciHeap is empty");
        Node<K> result = top;

        // 1. 把 top 的所有子节点加入根链表
        if (top.child != null) {
            Node<K> child = top.child;
            do {
                Node<K> next = child.right;
                child.parent = null;
                top = mergeLists(top, child);
                child = next;
            } while (child != result.child);
        }

        // 2. 从根链表移除 top
        removeFromList(result);
        size--;

        if (size == 0) {
            top = null;
        } else {
            top = result.right;
            consolidate();
        }
        return result.key;
    }

    public boolean isEmpty()          { return size == 0; }
    public int     size()             { return size; }
    public K       getHighestSpilled(){ return highestSpilled; }



    /** Returns true if a has higher priority than b. */
    private boolean better(K a, K b) {
        int c = comparator.compare(a, b);
        return max ? c > 0 : c < 0;
    }

    /** Merge two circular doubly-linked lists; return any node in the merged list. */
    private Node<K> mergeLists(Node<K> a, Node<K> b) {
        if (a == null) return b;
        if (b == null) return a;
        // splice b into a's list
        Node<K> aRight = a.right;
        a.right        = b.right;
        a.right.left   = a;
        b.right        = aRight;
        b.right.left   = b;
        return a;
    }

    /** Remove node from its circular list (does NOT update size). */
    private void removeFromList(Node<K> node) {
        node.left.right = node.right;
        node.right.left = node.left;
        node.left = node;
        node.right = node;
    }

    /** Link y under x (x has higher priority). */
    private void link(Node<K> y, Node<K> x) {
        removeFromList(y);
        y.parent = x;
        x.child  = mergeLists(x.child, y);
        x.degree++;
        y.marked = false;
    }

    /** Consolidate root list so no two roots have the same degree. */
    private void consolidate() {
        int maxDeg = (int) (Math.log(size) / Math.log(2)) + 2;
        @SuppressWarnings("unchecked")
        Node<K>[] table = new Node[maxDeg + 1];

        // collect root list
        List<Node<K>> roots = new ArrayList<>();
        Node<K> cur = top;
        do {
            roots.add(cur);
            cur = cur.right;
        } while (cur != top);

        for (Node<K> w : roots) {
            Node<K> x = w;
            int d = x.degree;
            while (d <= maxDeg && table[d] != null) {
                Node<K> y = table[d];
                if (better(y.key, x.key)) { Node<K> tmp = x; x = y; y = tmp; }
                link(y, x);
                table[d] = null;
                d++;
            }
            if (d <= maxDeg) table[d] = x;
        }

        // rebuild top
        top = null;
        for (Node<K> node : table) {
            if (node == null) continue;
            node.left = node.right = node;
            top = mergeLists(top, node);
            if (better(node.key, top.key)) top = node;
        }
    }
}