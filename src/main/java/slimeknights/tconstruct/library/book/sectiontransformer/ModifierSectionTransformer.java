package slimeknights.tconstruct.library.book.sectiontransformer;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.tconstruct.library.TinkerRegistries;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;

public class ModifierSectionTransformer extends ContentListingSectionTransformer {
  public static final ModifierSectionTransformer INSTANCE = new ModifierSectionTransformer("modifiers");

  public ModifierSectionTransformer(String name) {
    super(name);
  }

  @Override
  protected void processPage(BookData book, ContentListing listing, PageData page) {
    if (page.content instanceof ContentModifier) {
      ModifierId modifierId = new ModifierId(((ContentModifier) page.content).modifierID);
      if (TinkerRegistries.MODIFIERS.containsId(modifierId)) {
        Modifier modifier = TinkerRegistries.MODIFIERS.get(modifierId);
        assert modifier != null; // contains key was true
        listing.addEntry(modifier.getDisplayName().getString(), page);
      }
    } else {
      super.processPage(book, listing, page);
    }
  }
}
