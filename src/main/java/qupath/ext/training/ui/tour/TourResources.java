package qupath.ext.training.ui.tour;

import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

class TourResources {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.training.ui.tour");

    /**
     * Call the getString method of the resource bundle directly.
     * @param key
     * @return
     */
    public static String getString(String key) {
        return resources.getString(key);
    }

    private static String getTitleKey(String key) {
        return key + ".title";
    }

    /**
     * Request a tour item title for a given string.
     * The key will be updated to append '.title', so that the same key to be used to request both the title and
     * the text.
     * @param key
     * @return
     */
    public static String getTitle(String key) {
        var titleKey = getTitleKey(key);
        return resources.containsKey(titleKey) ? resources.getString(titleKey) : null;
    }

    private static String getTextKey(String key) {
        return key + ".text";
    }

    /**
     * Request tour item text for a given string.
     * The key will be updated to append '.text', so that the same key to be used to request both the title and
     * the text.
     * @param key
     * @return
     */
    public static String getText(String key) {
        var textKey = getTextKey(key);
        return resources.keySet()
                .stream()
                .filter(k -> k.startsWith(textKey))
                .sorted(Comparator.comparingInt(String::length))
                .map(TourResources::getUpdatedString)
                .collect(Collectors.joining("\n\n"));
    }

    private static String getUpdatedString(String key) {
        var s = getString(key);
        if (key.contains(".text.tip"))
            return "> **Tip:** " + s.replaceAll("\n", "\n> ");
        if (key.contains(".text.info"))
            return "> **Info:** " + s.replaceAll("\n", "\n> ");
        if (key.contains(".text.caution"))
            return "> **Caution:** " + s.replaceAll("\n", "\n> ");
        return s;
    }

}
