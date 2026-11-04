package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PageSwipeDynamicsTest {
  @Test
  public void usesOriginalDampingAndMinimumPageCount() {
    PageSwipeDynamics pageSwipe = new PageSwipeDynamics();
    pageSwipe.setMinPagesToSwipe(3);
    pageSwipe.setPageSwipe(.5f, .25f);

    pageSwipe.update(.1f);

    assertEquals(.15f, pageSwipe.getPageOffset(), .0001f);
  }
}
