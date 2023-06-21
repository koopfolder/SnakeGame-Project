package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GameMain extends JPanel {
	private static final long serialVersionUID = 1L;
	private Food food;
	private Snake snake;
	private GameCanvas pit;
	private ControlPanel control;
	private JLabel lblScore;
	static final String TITLE = "Snake Game by 6310742728";
	static final int ROWS = 25;
	static final int COLUMNS = 25;
	static final int CELL_SIZE = 20;
	static final int CANVAS_WIDTH = COLUMNS * CELL_SIZE;
	static final int CANVAS_HEIGHT = ROWS * CELL_SIZE;
	static final int UPDATE_PER_SEC = 3;
	static final long UPDATE_PERIOD_NSEC = 1000000000L / UPDATE_PER_SEC;

	enum GameState {
		INITIALIZED, PLAYING, PAUSED, GAMEOVER, DESTROYED
	}

	static JMenuBar menuBar;
	static GameState state;
	int score = 0;

	public GameMain() {
		gameInit();
		setLayout(new BorderLayout());
		pit = new GameCanvas();
		pit.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		add(pit, BorderLayout.CENTER);
		control = new ControlPanel();
		add(control, BorderLayout.SOUTH);
		setupMenuBar();
		gameStart();
	}

	public void gameInit() {
		snake = new Snake();
		food = new Food();
		state = GameState.INITIALIZED;
	}

	public void gameShutdown() {
	}

	public void gameStart() {
		Thread gameThread = new Thread() {
			public void run() {
				gameLoop();
			}
		};
		gameThread.start();
	}

	private void gameLoop() {
		if (state == GameState.INITIALIZED || state == GameState.GAMEOVER) {
			snake.regenerate();
			int x, y;
			do {
				food.regenerate();
				x = food.getX();
				y = food.getY();
			} while (snake.contains(x, y));
			state = GameState.PLAYING;
		}
		long beginTime, timeTaken, timeLeft;
		while (state != GameState.GAMEOVER) {
			beginTime = System.nanoTime();
			if (state == GameState.PLAYING) {
				gameUpdate();
			}
			repaint();
			timeTaken = System.nanoTime() - beginTime;
			timeLeft = (UPDATE_PERIOD_NSEC - timeTaken) / 1000000;
			if (timeLeft < 10)
				timeLeft = 10;
			try {
				Thread.sleep(timeLeft);
			} catch (InterruptedException ex) {
			}
		}
	}

	public void gameUpdate() {
		snake.update();
		processCollision();
	}

	public void processCollision() {
		int headX = snake.getHeadX();
		int headY = snake.getHeadY();

		if (headX == food.getX() && headY == food.getY()) {
			SoundEffect.EAT.play();
			score = score + 10;
			lblScore.setText("Score: " + score);
			int x, y;
			do {
				food.regenerate();
				x = food.getX();
				y = food.getY();
			} while (snake.contains(x, y));
		} else {
			snake.shrink();
		}
		if (!pit.contains(headX, headY)) {
			state = GameState.GAMEOVER;
			SoundEffect.DIE.play();
			JLabel label = new JLabel("GAME OVER!\n\nYour score is: " + score);
			label.setForeground(Color.RED);
			Font font = new Font("Verdana", Font.BOLD, 30);
			label.setFont(font);
			JOptionPane.showMessageDialog(null, label, "Game Over", JOptionPane.PLAIN_MESSAGE);
			score = 0;
			lblScore.setText("Score: " + score);
			return;
		}
		if (snake.eatItself()) {
			state = GameState.GAMEOVER;
			SoundEffect.DIE.play();
			JLabel label = new JLabel("GAME OVER!\n\nYour score is: " + score);
			label.setForeground(Color.RED);
			Font font = new Font("Verdana", Font.BOLD, 30);
			label.setFont(font);
			JOptionPane.showMessageDialog(null, label, "Game Over", JOptionPane.PLAIN_MESSAGE);
			score = 0;
			lblScore.setText("Score: " + score);
			return;
		}
	}

	private void gameDraw(Graphics g) {
		snake.draw(g);
		food.draw(g);

		g.setFont(new Font("Dialog", Font.PLAIN, 16));
		g.setColor(Color.BLACK);
		g.drawString("Score: " + (score), 400, 25);

		if (state == GameState.GAMEOVER) {
			String message = "GAME OVER!Your score is: " + score;
			JLabel label = new JLabel(message);
			label.setForeground(Color.RED);
			Font font = new Font("Verdana", Font.BOLD, 30);
			label.setFont(font);
			JOptionPane.showMessageDialog(null, label, "Game Over", JOptionPane.PLAIN_MESSAGE);
		}
	}

	public void gameKeyPressed(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_UP:
			snake.setDirection(Snake.Direction.UP);
			break;
		case KeyEvent.VK_DOWN:
			snake.setDirection(Snake.Direction.DOWN);
			break;
		case KeyEvent.VK_LEFT:
			snake.setDirection(Snake.Direction.LEFT);
			break;
		case KeyEvent.VK_RIGHT:
			snake.setDirection(Snake.Direction.RIGHT);
			break;
		}
	}

	class ControlPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JButton btnStartPause;
		private JButton btnStop;
		private JButton btnMute;
		private ImageIcon iconStart = new ImageIcon(getClass().getResource("/images/start.png"), "START");
		private ImageIcon iconPause = new ImageIcon(getClass().getResource("/images/pause.png"), "PAUSE");
		private ImageIcon iconStop = new ImageIcon(getClass().getResource("/images/stop.png"), "STOP");
		private ImageIcon iconSound = new ImageIcon(getClass().getResource("/images/sound.png"), "SOUND ON");
		private ImageIcon iconMuted = new ImageIcon(getClass().getResource("/images/muted.png"), "MUTED");

		public ControlPanel() {
			this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
			btnStartPause = new JButton(iconPause);
			btnStartPause.setToolTipText("Pause");
			btnStartPause.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnStartPause.setEnabled(true);
			add(btnStartPause);

			btnStop = new JButton(iconStop);
			btnStop.setToolTipText("Stop");
			btnStop.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnStop.setEnabled(true);
			add(btnStop);

			btnMute = new JButton(iconMuted);
			btnMute.setToolTipText("Mute");
			btnMute.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnMute.setEnabled(true);
			add(btnMute);

			lblScore = new JLabel("Score: 0");
			lblScore.setForeground(Color.decode("#EEEEEE"));
			add(lblScore);

			btnStartPause.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switch (state) {
					case INITIALIZED:
					case GAMEOVER:
						btnStartPause.setIcon(iconPause);
						btnStartPause.setToolTipText("Pause");
						gameStart();
						SoundEffect.CLICK.play();
						score = 0;
						lblScore.setText("Score: " + score);
						break;
					case PLAYING:
						state = GameState.PAUSED;
						btnStartPause.setIcon(iconStart);
						btnStartPause.setToolTipText("Start");
						SoundEffect.CLICK.play();
						break;
					case PAUSED:
						state = GameState.PLAYING;
						btnStartPause.setIcon(iconPause);
						btnStartPause.setToolTipText("Pause");
						SoundEffect.CLICK.play();
						break;
					}
					btnStop.setEnabled(true);
					pit.requestFocus();
				}
			});

			btnStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					state = GameState.GAMEOVER;
					btnStartPause.setIcon(iconStart);
					btnStartPause.setEnabled(true);
					btnStop.setEnabled(false);
					SoundEffect.CLICK.play();
				}
			});

			btnMute.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (SoundEffect.volume == SoundEffect.Volume.MUTE) {
						SoundEffect.volume = SoundEffect.Volume.LOW;
						btnMute.setIcon(iconSound);
						SoundEffect.CLICK.play();
						pit.requestFocus();
					} else {
						SoundEffect.volume = SoundEffect.Volume.MUTE;
						btnMute.setIcon(iconMuted);
						SoundEffect.CLICK.play();
						pit.requestFocus();
					}

				}
			});

		}

		public void reset() {
			btnStartPause.setIcon(iconStart);
			btnStartPause.setEnabled(true);
			btnStop.setEnabled(false);
		}
	}

	class GameCanvas extends JPanel implements KeyListener {
		private static final long serialVersionUID = 1L;

		public GameCanvas() {
			setFocusable(true);
			requestFocus();
			addKeyListener(this);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			setBackground(Color.decode("0xD7FF65"));
			gameDraw(g);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			gameKeyPressed(e.getKeyCode());
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		public boolean contains(int x, int y) {
			if ((x < 0) || (x >= ROWS))
				return false;
			if ((y < 0) || (y >= COLUMNS))
				return false;
			return true;
		}
	}

	private void setupMenuBar() {
		JMenu menu;
		JMenuItem menuItem;
		menuBar = new JMenuBar();
		menu = new JMenu("Game");
		menu.setMnemonic(KeyEvent.VK_G);
		menuBar.add(menu);
		menuItem = new JMenuItem("New Game", KeyEvent.VK_N);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (state == GameState.PLAYING || state == GameState.PAUSED) {
					state = GameState.GAMEOVER;
				}
				gameStart();
				control.reset();
			}
		});
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(TITLE);
				frame.setContentPane(new GameMain());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setJMenuBar(menuBar);
				frame.setVisible(true);
			}
		});
	}
}
