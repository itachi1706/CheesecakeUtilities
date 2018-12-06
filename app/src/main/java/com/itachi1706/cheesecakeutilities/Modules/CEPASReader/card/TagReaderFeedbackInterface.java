
package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.CardInfo;

/**
 * Allows card reader protocols to talk back to the ReadingTagActivity, and indicate to the user
 * what progress has been made on reading their card.
 */

public interface TagReaderFeedbackInterface {
    /**
     * Show some message to the user indicating what is happening.
     *
     * This value should be localised by the caller.
     * @param msg Localised message to display to the user.
     */
    void updateStatusText(final String msg);

    /**
     * Signal to update the progress bar drawn on screen.
     *
     * If both progress and max are set to 0, then this will show an "indeterminate" (spinning)
     * progress bar instead.
     *
     * @param progress Current position in the reading operation.
     * @param max Position when we have reached completion.
     */
    void updateProgressBar(final int progress, final int max);

    /**
     * Some readers may be able to determine a card type early on in the process, without having
     * dumped all of the data from the card yet.
     *
     * In this case, we can be sent a CardInfo describing the type of card, so that we can show a
     * nice image to reassure the user that we're hard at work.
     *
     * This should only ever be called once.
     *
     * This is also used as a trigger for accessibility tools, and announces what type of card is
     * being read (from the last status update). If the card type is not likely to be known quickly,
     * then sending a 'null' CardInfo will also trigger those accessibility features.
     * @param cardInfo Card information to display.
     */
    void showCardType(final CardInfo cardInfo);
}
