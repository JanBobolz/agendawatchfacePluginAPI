package de.janbo.agendawatchface.api;

/**
 * Options for handling long texts
 * @author Jan
 *
 */
public enum LineOverflowBehavior {
	/**
	 * The line always has the same height, regardless of what text is displayed
	 */
	NONE,
	
	/**
	 * For long texts, line height is increased
	 */
	OVERFLOW_IF_NECESSARY,
	
	/**
	 * The line always has twice the normal height, regardless of what text is displayed
	 */
	ALWAYS_TWICE_THE_HEIGHT
}
