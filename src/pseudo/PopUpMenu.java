package pseudo;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class PopUpMenu extends JPanel {

  public JPopupMenu popup;

  public PopUpMenu() {
    popup = new JPopupMenu();
    ActionListener menuListener =   new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent event) {
        System.out.println("Popup menu item ["
            + event.getActionCommand() + "] was pressed.");
      }
    };
    JMenuItem item;
    popup.add(item = new JMenuItem("Left", new ImageIcon("1.gif")));
    item.setHorizontalTextPosition(SwingConstants.RIGHT);
    item.addActionListener(menuListener);
    popup.add(item = new JMenuItem("Center", new ImageIcon("2.gif")));
    item.setHorizontalTextPosition(SwingConstants.RIGHT);
    item.addActionListener(menuListener);
    popup.add(item = new JMenuItem("Right", new ImageIcon("3.gif")));
    item.setHorizontalTextPosition(SwingConstants.RIGHT);
    item.addActionListener(menuListener);
    popup.add(item = new JMenuItem("Full", new ImageIcon("4.gif")));
    item.setHorizontalTextPosition(SwingConstants.RIGHT);
    item.addActionListener(menuListener);
    popup.addSeparator();
    popup.add(item = new JMenuItem("Settings . . ."));
    item.addActionListener(menuListener);

    popup.setLabel("Justification");
    popup.setBorder(new BevelBorder(BevelBorder.RAISED));
    popup.addPopupMenuListener(new PopupPrintListener());

    addMouseListener(new MousePopupListener());
    
  }

  // An inner class to check whether mouse events are the popup trigger
  class MousePopupListener extends MouseAdapter {
    @Override
	public void mousePressed(MouseEvent e) {
      checkPopup(e);
    }

    @Override
	public void mouseClicked(MouseEvent e) {
      checkPopup(e);
    }

    @Override
	public void mouseReleased(MouseEvent e) {
      checkPopup(e);
    }

    private void checkPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popup.show(PopUpMenu.this, e.getX(), e.getY());
      }
    }
  }

  // An inner class to show when popup events occur
  class PopupPrintListener implements PopupMenuListener {
    @Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      System.out.println("Popup menu will be visible!");
    }

    @Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      System.out.println("Popup menu will be invisible!");
    }

    @Override
	public void popupMenuCanceled(PopupMenuEvent e) {
      System.out.println("Popup menu is hidden!");
    }
  }

}

           
         