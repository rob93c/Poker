package pet.ui.eq;

import pet.eq.*;

/**
 * calc panel for draw hands
 */
public class DrawCalcPanel extends CalcPanel {
	
	public DrawCalcPanel() {
		super(true);
		HandCardPanel[] handPanels = new HandCardPanel[6];
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Draw hand " + (n + 1), 1, 5, false);
		}
		setHandCardPanels(handPanels);
		
		initCardLabels();
		
		PokerItem[] items = new PokerItem[] {
				new PokerItem(PokerItem.HIGH, new DrawPoker(Value.hiValue)),
				new PokerItem(PokerItem.AFLOW, new DrawPoker(Value.afLowValue)),
				new PokerItem(PokerItem.DSLOW, new DrawPoker(Value.dsLowValue)),
		};
		setPokerItems(items);
		
		selectCard(0);
	}
		
}
