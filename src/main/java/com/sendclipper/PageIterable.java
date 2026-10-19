package com.sendclipper;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Every item of a list that spans pages, fetched lazily page after page.
 *
 * <pre>{@code
 * for (EmailRow email : client.emails().all(params)) { ... }
 * client.suppressions().all(null).stream().filter(...)
 * }</pre>
 *
 * <p>Each iteration starts again from the first page. A page that fails throws its {@link
 * SendClipperException} from {@code hasNext()}.
 *
 * @param <T> the item type
 */
public final class PageIterable<T> implements Iterable<T> {
  private final Supplier<Iterator<T>> start;

  /**
   * Wraps a source of iterators. The SDK makes these.
   *
   * @param start returns a fresh iterator from the first page
   */
  public PageIterable(Supplier<Iterator<T>> start) {
    this.start = start;
  }

  @Override
  public Iterator<T> iterator() {
    return start.get();
  }

  /**
   * The items as a sequential stream.
   *
   * @return a stream that fetches pages as it is consumed
   */
  public Stream<T> stream() {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED | Spliterator.NONNULL),
        false);
  }
}
