package pet.ui.hp;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import pet.PET;
import pet.hp.*;
import pet.hp.state.*;
import pet.ui.PokerFrame;
import pet.ui.eq.CalcPanel;
import pet.ui.eq.PokerItem;
import pet.ui.table.*;

/**
 * displays a table with details for the last hand
 */
public class LastHandPanel extends JPanel implements HistoryListener {
	
	private final JComboBox<HandStateItem> stateCombo = new JComboBox<>(new DefaultComboBoxModel<HandStateItem>());
	private final MyJTable handTable = new MyJTable();
	private final JButton prevButton = new JButton(PokerFrame.LEFT_TRI);
	private final JButton nextButton = new JButton(PokerFrame.RIGHT_TRI);
	private final JButton equityButton = new JButton("Equity");
	private final JButton playerButton = new JButton("Player");
	private final JButton replayButton = new JButton("Replay");
	private final JToggleButton autoButton = new JToggleButton("Auto");
	private final JScrollPane tableScroller = new JScrollPane(handTable);

	public LastHandPanel() {
		super(new BorderLayout());

		stateCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				HandStateItem states = (HandStateItem) e.getItem();
				tableScroller.setBorder(new TitledBorder(states.hand.game.id));
				((HandStateTableModel)handTable.getModel()).setRows(states.states);
				updateAuto();
			}
		});

		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectState(-1);
			}
		});

		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectState(1);
			}
		});
		
		playerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get player name for selected row
				int r = handTable.getSelectionModel().getMinSelectionIndex();
				if (r >= 0) {
					// not really needed, as table can't be sorted
					int sr = handTable.convertRowIndexToModel(r);
					HandStateTableModel m = (HandStateTableModel) handTable.getModel();
					HandState hs = m.getRow(sr);
					SeatState as = hs.actionSeat();
					if (as != null) {
						String player = as.seat.name;
						PET.getPokerFrame().displayPlayer(player);
					}
				}
			}
		});

		equityButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HandStateItem hs = (HandStateItem) stateCombo.getSelectedItem();
				String type;
				switch (hs.hand.game.type) {
					case HE:
					case OM:
					case FCD:
					case STUD:
					case FCSTUD:
					case OM5:
					case OM51:
						type = PokerItem.HIGH;
						break;
					case OMHL:
					case STUDHL:
					case OM51HL:
					case OM5HL:
						type = PokerItem.HILO;
						break;
					case DSSD:
					case DSTD:
						type = PokerItem.DSLOW;
						break;
					case RAZZ:
					case AFTD:
						type = PokerItem.AFLOW;
						break;
					case BG:
						type = PokerItem.BADUGI;
					default:
						throw new RuntimeException("unknown game type " + hs.hand);
				}
				
				PokerFrame pf = PET.getPokerFrame();
				CalcPanel calcPanel = pf.displayCalcPanel(hs.hand.game.type);
				// TODO maybe take hands for selected street instead
				List<String[]> cards = HandUtil.getFinalCards(hs.hand);
				calcPanel.displayHand(hs.hand.board, cards, type);
			}
		});

		replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HandStateItem hs = (HandStateItem) stateCombo.getSelectedItem();
				if (hs != null) {
					PET.getPokerFrame().replayHand(hs.hand);
				}
			}
		});
		
		handTable.setModel(new HandStateTableModel());
		handTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		autoButton.setSelected(true);
		
		JPanel topPanel = new JPanel();
		topPanel.add(prevButton);
		topPanel.add(stateCombo);
		topPanel.add(nextButton);
		topPanel.add(autoButton);

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(equityButton);
		bottomPanel.add(playerButton);
		bottomPanel.add(replayButton);
		
		add(topPanel, BorderLayout.NORTH);
		add(tableScroller, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}


	private void selectState(int off) {
		int i = stateCombo.getSelectedIndex();
		if (i >= 0) {
			i += off;
			if (i >= 0 && i < stateCombo.getItemCount()) {
				System.out.println("setting index " + i);
				stateCombo.setSelectedIndex(i);
				updateAuto();
				repaint();
			}
		}
	}
	
	private void updateAuto() {
		// set auto if on last item
		autoButton.setSelected(stateCombo.getSelectedIndex() == stateCombo.getItemCount() - 1);
	}
	
	@Override
	public void handAdded(final Hand hand) {
		long t = System.currentTimeMillis() - (1000 * 60 * 10);
		if (hand.date > t) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showHand(hand);
				}
			});
		}
	}

	@Override
	public void gameAdded(Game game) {
		//
	}

	public void showHand(final Hand hand) {
		// create hand states, add to list
		// display most recent in hud
		final DefaultComboBoxModel<HandStateItem> model = (DefaultComboBoxModel<HandStateItem>) stateCombo.getModel();

		for (int n = 0; n < model.getSize(); n++) {
			HandStateItem hs = model.getElementAt(n);
			if (hs.hand == hand) {
				// already present, just select
				stateCombo.setSelectedIndex(n);
				return;
			}
		}
		
		// TODO display "please wait" or something
		
		// do equity calculation on non-awt thread
		// don't use executor service because it doesn't tell you if an
		// exception has occurred, or at least, not without calling get()
		// (synchronously), which is stupid as the whole point is to avoid a
		// synchronous call
		Thread t = new Thread("last hand thread") {
			@Override
			public void run() {
				try {
					System.out.println("last hand panel: getting hand states on background thread");
					final HandStateItem states = new HandStateItem(hand);
					System.out.println("last hand panel: got hand states " + states);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (model.getSize() > 100) {
								model.removeElementAt(0);
							}
							model.addElement(states);
							if (autoButton.isSelected()) {
								stateCombo.setSelectedIndex(stateCombo.getModel().getSize() - 1);
							}
						}
					});
				} catch (Exception e) {
					PET.handleException(Thread.currentThread().getName(), e);
				}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.setDaemon(true);
		t.start();
	}
	
}

/** represents a list of hand states for a hand */
class HandStateItem {
	public final List<HandState> states;
	public final Hand hand;
	public HandStateItem(Hand hand) {
		this.hand = hand;
		this.states = HandStateUtil.getStates(hand);
	}
	@Override
	public String toString() {
		// user readable description of hand
		return hand.tablename + " " + DateFormat.getDateTimeInstance().format(new Date(hand.date)) + (hand.showdown ? " *" : "");
	}
}
