package com.sendclipper.core;

import com.sendclipper.PageIterable;
import com.sendclipper.model.CursorInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.IntFunction;

/** Turns page fetchers into {@link PageIterable}s. Internal: not part of the public API. */
public final class Paging {
  private Paging() {}

  /** One numbered page: its items and whether it is the last. */
  public static final class Numbered<T> {
    final List<T> items;
    final boolean last;

    /**
     * A page.
     *
     * @param items its items (null reads as none)
     * @param last whether no page follows
     */
    public Numbered(List<T> items, boolean last) {
      this.items = items == null ? Collections.emptyList() : items;
      this.last = last;
    }
  }

  /** One cursor page: its items and the paging block. */
  public static final class Cursored<T> {
    final List<T> items;
    final CursorInfo page;

    /**
     * A page.
     *
     * @param items its items (null reads as none)
     * @param page the paging block (null reads as the last page)
     */
    public Cursored(List<T> items, CursorInfo page) {
      this.items = items == null ? Collections.emptyList() : items;
      this.page = page;
    }
  }

  /**
   * Walks numbered pages from 1.
   *
   * @param <T> the item type
   * @param fetch returns the page with that number
   * @return the items
   */
  public static <T> PageIterable<T> numbered(IntFunction<Numbered<T>> fetch) {
    return new PageIterable<>(
        () ->
            new Walker<T>() {
              int page = 1;

              @Override
              List<T> next(boolean first) {
                if (!first && last) {
                  return null;
                }
                Numbered<T> res = fetch.apply(page++);
                last = res.last || res.items.isEmpty();
                return res.items;
              }

              boolean last;
            });
  }

  /**
   * Follows a cursor from the first page.
   *
   * @param <T> the item type
   * @param fetch returns the page at that cursor (null for the first)
   * @return the items
   */
  public static <T> PageIterable<T> cursor(Function<String, Cursored<T>> fetch) {
    return new PageIterable<>(
        () ->
            new Walker<T>() {
              String cursor;
              boolean done;

              @Override
              List<T> next(boolean first) {
                if (done) {
                  return null;
                }
                Cursored<T> res = fetch.apply(cursor);
                CursorInfo info = res.page;
                cursor = info == null ? null : info.getCursor();
                done = info == null || !info.hasMore() || cursor == null || cursor.isEmpty();
                return res.items;
              }
            });
  }

  private abstract static class Walker<T> implements Iterator<T> {
    private Iterator<T> current = Collections.emptyIterator();
    private boolean started;
    private boolean exhausted;

    /** The next page's items, or null when there is none. */
    abstract List<T> next(boolean first);

    @Override
    public boolean hasNext() {
      while (!current.hasNext()) {
        if (exhausted) {
          return false;
        }
        List<T> items = next(!started);
        started = true;
        if (items == null) {
          exhausted = true;
          return false;
        }
        current = items.iterator();
      }
      return true;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return current.next();
    }
  }
}
