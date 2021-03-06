package client.gui;

import java.awt.*;
import javax.swing.*;
import java.util.Stack;
import java.util.Vector;

/**
 * This panel displays CardComponents one at a time
 * @author dvanhumb
 */
public class CardPanel extends JPanel
{
	private static final long serialVersionUID = 0x92834;
	
	private CardLayout cardLayout;
	private Vector<String> cardNames;
	private Stack<String> cardStack;
	
	/**
	 * Create a new CardPanel
	 * @param defaultCard  The default card to show (can also be called the "root" card)
	 */
	public CardPanel(CardComponent defaultCard)
	{
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		
		cardNames = new Vector<String>();
		cardStack = new Stack<String>();
		
		if (defaultCard != null)
			showCard(defaultCard);
	}
	
	public boolean isChild(CardComponent c)
	{
		return cardNames.contains(c.getCardName());
	}
	
	public void add(CardComponent c)
	{
		super.add(c);
		cardNames.add(c.getCardName());
	}
	
	public void showCard(CardComponent c)
	{
		// If we don't contain this component, add it, revalidate ourselves, then show the component
		if (!isChild(c))
		{
			add(c);
			cardLayout.addLayoutComponent(c, c.getCardName());
			if (isVisible())
				validate();
		}
		
		cardLayout.show(this, c.getCardName());
		cardStack.push(c.getCardName());
	}
	
	/**
	 * Hide the topmost card in the stack, if there is one
	 */
	public void hideCard()
	{
		if (cardStack.size() == 0)
			return;
		cardStack.pop();
		cardLayout.show(this, cardStack.peek());
	}
}
