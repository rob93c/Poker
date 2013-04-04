package pet.hp;

import java.util.*;

import pet.eq.Poker;

/**
 * Utilities for hands (no analysis - see HandInfo)
 */
public class HandUtil {
	
	/**
	 * Compare hands by id, this is equivalent to sorting by date
	 */
	public static final Comparator<Hand> idCmp = new Comparator<Hand>() {
		@Override
		public int compare(Hand h1, Hand h2) {
			int c = h1.id.compareTo(h2.id);
			return c;
		}
	};
	
	/**
	 * compare by seat number
	 */
	public static final Comparator<Seat> seatCmp = new Comparator<Seat>() {
		@Override
		public int compare(Seat s1, Seat s2) {
			return s1.num - s2.num;
		}
	};
	
	/**
	 * get board for street (index from 0).
	 * return null if the game doesn't use a board
	 */
	public static String[] getStreetBoard(Hand hand, int streetIndex) {
		switch (hand.game.type) {
			case FCD:
			case DSTD:
			case DSSD:
			case FCSTUD:
			case BG:
			case AFTD:
				return null;
			case HE:
			case OM:
			case OMHL:
			case OM5:
			case OM5HL:
				return streetIndex > 0 ? Arrays.copyOf(hand.board, streetIndex + 2) : Poker.emptyBoard;
			case OM51:
			case OM51HL:
				return Arrays.copyOf(hand.board, streetIndex == 0 ? 1 : streetIndex + 2);
			case STUD:
			case RAZZ:
			case STUDHL:
				return streetIndex == 4 && hand.board.length > 0 ? hand.board : Poker.emptyBoard;
			default:
				throw new RuntimeException("unknown game type " + hand.game.type);
		}
	}

	/**
	 * get the final cards this seat had for display purposes returns null if no
	 * known cards, array may contain null if some are unknown
	 */
	public static String[] getFinalCards(Game.Type gametype, Seat seat) {
		switch (gametype) {
			case STUD:
			case STUDHL:
			case RAZZ: {
				String[] holeCards = seat.downCards;
				String[] upCards = seat.upCards;
				if (holeCards == null && upCards == null) {
					return null;
				}
				String[] cards = new String[7];
				if (holeCards != null) {
					cards[0] = holeCards.length > 0 ? holeCards[0] : null;
					cards[1] = holeCards.length > 1 ? holeCards[1] : null;
					cards[6] = holeCards.length > 2 ? holeCards[2] : null;
				}
				if (upCards != null) {
					cards[2] = upCards.length > 0 ? upCards[0] : null;
					cards[3] = upCards.length > 1 ? upCards[1] : null;
					cards[4] = upCards.length > 2 ? upCards[2] : null;
					cards[5] = upCards.length > 3 ? upCards[3] : null;
				}
				return cards;
			}
			
			case AFTD:
			case BG:
			case DSSD:
			case DSTD:
			case FCD:
			case HE:
			case OM:
			case OM5:
			case OM51:
			case OM51HL:
			case OM5HL:
			case OMHL:
				// TODO sort them?
				return seat.downCards;
				
			default:
				throw new RuntimeException();
		}
	}
	
	/**
	 * Get all final hole cards for hand and the blockers
	 */
	public static List<String[]> getFinalCards(Hand hand) {
		List<String[]> cardsList = new ArrayList<>();
		for (Seat seat : hand.seats) {
			String[] cards = getFinalCards(hand.game.type, seat);
			if (cards != null) {
				cardsList.add(cards);
			}
		}
		return cardsList;
	}
	
	private HandUtil() {
		//
	}
	
}
