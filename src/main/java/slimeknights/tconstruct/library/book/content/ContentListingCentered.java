package slimeknights.tconstruct.library.book.content;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import slimeknights.mantle.client.book.action.protocol.ProtocolGoToPage;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.tconstruct.library.book.TinkerPage;
import slimeknights.tconstruct.library.book.elements.ListingCenteredElement;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ContentListingCentered extends TinkerPage {

  private String title;
  private final List<TextData> entries = Lists.newArrayList();

  public void addEntry(String text, PageData link) {
    TextData data = new TextData(text);
    if (link != null) {
      data.action = ProtocolGoToPage.GO_TO_RTN + ":" + link.parent.name + "." + link.name;
    }
    this.entries.add(data);
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int yOff = 0;
    if (this.title != null) {
      this.addTitle(list, this.title, false);
      yOff = 20;
    }

    int y = yOff;
    int x = 0;
    int w = BookScreen.PAGE_WIDTH;

    for (TextData data : this.entries) {
      int ex = x + w / 2 - book.fontRenderer.getWidth(data.text) / 2;

      list.add(new ListingCenteredElement(ex, y, w, 9, data));
      y += 9;
    }
  }
}
