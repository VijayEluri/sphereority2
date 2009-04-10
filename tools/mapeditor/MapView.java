package tools.mapeditor;

import common.MapConstants;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Scanner;
import javax.swing.*;

/**
 * This displays an EditableMap to edit.
 * @author dvanhumb
 */
public class MapView extends JComponent implements MapConstants, MapAlterationListener, MouseListener, MouseMotionListener, ActionListener, ClipboardOwner
{
	private static final long serialVersionUID = 98234532L;
	
	private EditableMap map;
	private int zoomLevel;
	private Rectangle selectedRect;
	private String currentSelection;
	private int pinned_x = 0, pinned_y = 0;
	private JPopupMenu popupMenu;
	private JMenuItem menuCopy, menuPaste, menuSpace, menuWall, menuSpawnA, menuSpawnB, menuFlagA, menuFlagB;
	
	public MapView()
	{
		zoomLevel = 16;
		selectedRect = null;
		currentSelection = null;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		
		// Our popup menu
		menuCopy = createMenuItem("Copy", 0);
		menuPaste = createMenuItem("Paste", 0);
		menuSpace = createMenuItem("Space", 0, CHAR_SPACE);
		menuWall = createMenuItem("Wall", 0, CHAR_WALL);
		menuSpawnA = createMenuItem("Spawn (A)", -1, CHAR_SPAWN_A);
		menuSpawnB = createMenuItem("Spawn (B)", -1, CHAR_SPAWN_B);
		menuFlagA = createMenuItem("Flag (A)", -1, CHAR_FLAG_A);
		menuFlagB = createMenuItem("Flag (B)", -1, CHAR_FLAG_B);
		
		popupMenu = new JPopupMenu("Edit");
		popupMenu.add(menuCopy);
		popupMenu.add(menuPaste);
		popupMenu.addSeparator();
		popupMenu.add(menuSpace);
		popupMenu.add(menuWall);
		popupMenu.add(menuSpawnA);
		popupMenu.add(menuSpawnB);
		popupMenu.add(menuFlagA);
		popupMenu.add(menuFlagB);
		
		for (Component c : popupMenu.getComponents())
			c.setEnabled(false);
		setComponentPopupMenu(popupMenu);
	}
	
	private JMenuItem createMenuItem(String label, int mnemonic)
	{
		return createMenuItem(label, mnemonic, (char)0);
	}
	
	private JMenuItem createMenuItem(String label, int mnemonic, char c)
	{
		JMenuItem item;
		if (c > 0)
		{
			BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
			
			Graphics2D g = img.createGraphics();
			MapCellRenderer.renderCell(g, 0, 0, 15, 15, c);
			g.setColor(Color.black);
			g.drawRect(0, 0, 16, 16);
			g.dispose();
			
			item = new JMenuItem(label, new ImageIcon(img));
		}
		else
			item = new JMenuItem(label);
		
		if (mnemonic >= 0 && mnemonic < label.length())
			item.setMnemonic(label.charAt(mnemonic));
		item.addActionListener(this);
		
		return item;
	}
	
	private void updateSize()
	{
		if (map == null)
			return;
		
		Dimension d = new Dimension(zoomLevel*map.getWidth(), zoomLevel*map.getHeight());
		setSize(d);
		setPreferredSize(d);
		setMinimumSize(d);
		revalidate();
		repaint();
	}
	
	public void setMap(EditableMap map)
	{
		this.map = map;
		
		for (Component c : popupMenu.getComponents())
			c.setEnabled(map != null);
		
		updateSize();
	}
	
	public void setZoomLevel(int level)
	{
		if (level < 1)
			return;
		
		zoomLevel = level;
		
		updateSize();
	}
	
	public int getZoomLevel()
	{
		return zoomLevel;
	}
	
	public EditableMap getMap()
	{
		return map;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		
		// Figure out the bounds of the redraw rectangle
		Rectangle clipBounds = g2.getClipBounds();
		
		// If we don't have a map, draw the background blank and return
		if (map == null)
		{
			g2.setColor(getBackground());
			g2.fill(g2.getClip());
			return;
		}
		
		g2.setColor(Color.white);
		g2.fill(g2.getClip());
		
		int min_x, min_y, max_x, max_y;
		min_x = Math.max(clipBounds.x / zoomLevel - 1, 0);
		max_x = Math.min((clipBounds.x + clipBounds.width) / zoomLevel + 1, map.getWidth());
		min_y = Math.max(clipBounds.y / zoomLevel - 1, 0);
		max_y = Math.min((clipBounds.y + clipBounds.height) / zoomLevel + 1, map.getHeight());
		
		// Draw the map
		for (int y = min_y; y < max_y; y++)
			for (int x = min_x; x < max_x; x++)
				MapCellRenderer.renderCell(g2, x*zoomLevel, y*zoomLevel, zoomLevel, zoomLevel, map.getSquareType(x, y));
		
		// Draw a grid if we're zoomed in enough
		if (zoomLevel >= 8)
		{
			g2.setColor(Color.black);
			for (int x=min_x; x <= max_x; x++)
				g2.drawLine(x*zoomLevel, min_y*zoomLevel, x*zoomLevel, max_y*zoomLevel);
			for (int y=min_y; y <= max_y; y++)
				g2.drawLine(min_x*zoomLevel, y*zoomLevel, max_x*zoomLevel, y*zoomLevel);
		}
		
		// Do we have a selection?
		if (selectedRect != null)
		{
			// Make sure the selection is inside the current map
			
			// Draw the selection
			g2.setColor(Color.green);
			g2.drawRect(selectedRect.x*zoomLevel,
					selectedRect.y*zoomLevel,
					selectedRect.width*zoomLevel,
					selectedRect.height*zoomLevel);
		}
	}

	public void mapChanged(EditableMap map, int x, int y)
	{
//		if (map == this.map)
			repaintCell(x, y);
	}
	
	public void repaintCell(int x, int y)
	{
		repaint(zoomLevel*x, zoomLevel*y, zoomLevel+1, zoomLevel+1);
	}
	
	public void repaintCells(Rectangle rect)
	{
		if (rect != null)
			repaint(rect.x*zoomLevel, rect.y*zoomLevel, rect.width*zoomLevel + 1, rect.height*zoomLevel + 1);
	}
	
	public void repaintCells(int x, int y, int width, int height)
	{
		repaint(zoomLevel*x, zoomLevel*y, width*zoomLevel+1, height*zoomLevel+1);
	}
	
	public void mouseClicked(MouseEvent e)
	{
		// If it's not the primary mouse button, don't do anything
		if ((e.getButton() != MouseEvent.BUTTON1) || (map == null))
			return;
		
		int x = e.getX(), y = e.getY();
		x /= zoomLevel;
		y /= zoomLevel;
		x = Math.max(0, Math.min(map.getWidth()-1, x));
		y = Math.max(0, Math.min(map.getHeight()-1, y));
		
		if ((map == null) || (x < 0) || (x >= map.getWidth()) || (y < 0) || (y >= map.getHeight()))
			return;
		
		Rectangle rect = null;
		if (selectedRect != null)
			rect = new Rectangle(selectedRect);
		selectedRect = new Rectangle(x, y, 1, 1);
		
		repaintCells(rect);
		repaintCells(selectedRect);
	}

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) { }

	public void mousePressed(MouseEvent e)
	{
		// If it's not the primary mouse button, don't do anything
		if ((e.getButton() != MouseEvent.BUTTON1) || (map == null))
			return;
		
		int x = e.getX(), y = e.getY();
		x /= zoomLevel;
		y /= zoomLevel;
		x = Math.max(0, Math.min(map.getWidth()-1, x));
		y = Math.max(0, Math.min(map.getHeight()-1, y));
		pinned_x = x;
		pinned_y = y;
		
		Rectangle rect = null;
		if (selectedRect != null)
			rect = new Rectangle(selectedRect);
		selectedRect = new Rectangle(x, y, 1, 1);
		
		repaintCells(rect);
		repaintCells(selectedRect);
	}

	public void mouseReleased(MouseEvent e) { }

	public void mouseDragged(MouseEvent e)
	{
		// If it's not the primary mouse button, don't do anything
		if (map == null)
			return;
		
		int x = e.getX(), y = e.getY();
		x /= zoomLevel;
		y /= zoomLevel;
		x = Math.max(0, Math.min(map.getWidth()-1, x));
		y = Math.max(0, Math.min(map.getHeight()-1, y));
		
		int width = x - pinned_x, height = y - pinned_y;
		Rectangle rect = new Rectangle(selectedRect);
		
		if (width < 0)
			selectedRect.x = pinned_x + width;
		else
			selectedRect.x = pinned_x;
		if (height < 0)
			selectedRect.y = pinned_y + height;
		else
			selectedRect.y = pinned_y;
		
		selectedRect.width = 1 + Math.abs(width);
		selectedRect.height = 1 + Math.abs(height);
		
		if (!rect.equals(selectedRect))
		{
			repaintCells(rect);
			repaintCells(selectedRect);
		}
	}

	public void mouseMoved(MouseEvent e) { }
	
	/**
	 * Get what's currently selected
	 * @return  The current selection
	 */
	public String copySelection()
	{
		if (selectedRect == null)
			return null;
		
		String result = String.format("%d %d\n", selectedRect.width, selectedRect.height);
		for (int y=0; y < selectedRect.height; y++)
		{
			for (int x=0; x < selectedRect.width; x++)
				result += map.getSquareType(x + selectedRect.x, y + selectedRect.y);
			result += "\n";
		}
		
		return result;
	}
	
	/**
	 * Paste in a selection
	 * @param selection  The selection to paste in
	 */
	public void pasteSelection(String selection)
	{
		Scanner in = new Scanner(selection);
		int width = in.nextInt(), height = in.nextInt();
		in.nextLine();
		
		String line;
		for (int y=0; y < height; y++)
		{
			line = in.nextLine();
			for (int x=0; x < width; x++)
			{
				map.setSquareType(x+this.selectedRect.x, y+this.selectedRect.y, line.charAt(x));
			}
		}
		
		repaintCells(this.selectedRect.x, this.selectedRect.y, width, height);
	}
	
	public void fillSelectionWith(char c)
	{
		if (selectedRect == null)
			return;
		
		for (int y=0; y < selectedRect.height; y++)
			for (int x=0; x < selectedRect.width; x++)
				map.setSquareType(x + selectedRect.x, y + selectedRect.y, c);
		
		repaintCells(selectedRect);
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		
		if (source.equals(menuCopy))
		{
			copy();
		}
		else if (source.equals(menuPaste))
		{
			paste();
		}
		else if (source.equals(menuSpace))
		{
			fillSelectionWith(CHAR_SPACE);
		}
		else if (source.equals(menuWall))
		{
			fillSelectionWith(CHAR_WALL);
		}
		else if (source.equals(menuSpawnA))
		{
			fillSelectionWith(CHAR_SPAWN_A);
		}
		else if (source.equals(menuSpawnB))
		{
			fillSelectionWith(CHAR_SPAWN_B);
		}
		else if (source.equals(menuFlagA))
		{
			fillSelectionWith(CHAR_FLAG_A);
		}
		else if (source.equals(menuFlagB))
		{
			fillSelectionWith(CHAR_FLAG_B);
		}
	}
	
	public void copy()
	{
		String selection = copySelection();
		if (selection == null)
			return;
		
		// Put the selection in the clipboard
		currentSelection = selection;
		menuPaste.setEnabled(true);
		
		// System-wide clipboard not yet supported
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selection), this);
	}
	
	public void paste()
	{
		if ((map == null) || (currentSelection == null))
			return;
		
		pasteSelection(currentSelection);
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		// We don't happen to care if we loose ownership of the clipboard
	}
}
