package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * @author Faisal Jamil Student Number: 766747
 */
public class Main extends Application {

	// TODO Automatic message receiver
	// Check multi-threading (Non-blocking methods)
	// TODO Opposite chat color
	// TODO Send images, videos, files
	// TODO Scrollbar for chat

	// Creates a shape that covers the screen
	// Used for layout purposes
	private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

	private String serverPortString = null;
	private int serverPortInt = 0;
	private String joinIPAddress = null;
	private String joinPortString = null;
	private int joinPortInt = 0;

	private ServerSocket serverSocket;
	private Socket serverClientSocket;

	private Socket client;

	private DataOutputStream dos;
	private DataInputStream dis;

	private String userName = null;

	private String[] emoticonArray = { ":)", "LUL", "<3", "EZ", "SeemsGood", "monkaS", "WutFace", "monkaX", "PepeHands",
			"BibleThump", "RareBoi", "RareParrot", "VoteYay", "VoteNay", "Clap" };

	/**
	 * Displays the current time in the format: 12-Clock Hour:Minutes:Seconds AM/PM
	 * 
	 * @return The current time in the specified format as a string
	 */
	private String time() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
		String time = sdf.format(cal.getTime());
		return time;
	}

	// /**
	// * Generates a random number based on the parameters
	// *
	// * @param min
	// * The minimum integer
	// * @param max
	// * The maximum integer
	// * @return The randomly generated integer
	// */
	// private int randomWithRange(int min, int max) {
	// int range = (max - min) + 1;
	// return (int) (Math.random() * range) + min;
	// }

	@SuppressWarnings("static-access")
	@Override
	public void start(Stage primaryStage) {
		try {

			// Sets variable equal to video file path
			Media backgroundVideo = new Media(new File("res/background.mp4").toURI().toString());

			// Sets a video player to play the video
			MediaPlayer backgroundVideoPlayer = new MediaPlayer(backgroundVideo);

			// Runs indefinitely
			backgroundVideoPlayer.setCycleCount(MediaPlayer.INDEFINITE);
			backgroundVideoPlayer.play();
			MediaView backgroundVideoViewer = new MediaView(backgroundVideoPlayer);
			// Set size based on screen size
			backgroundVideoViewer.setFitWidth(primaryScreenBounds.getWidth());
			backgroundVideoViewer.setFitHeight(primaryScreenBounds.getHeight());

			// Layouts
			VBox vBox = new VBox();
			BorderPane borderPane = new BorderPane(vBox);
			StackPane group = new StackPane();
			group.getChildren().addAll(backgroundVideoViewer, borderPane);
			Scene titleScene = new Scene(group);
			titleScene.setFill(Color.BLACK);

			Text title = new Text("...Hello");
			Text start = new Text("Press any button to continue");

			vBox.getChildren().add(title);
			vBox.getChildren().add(start);

			vBox.setSpacing(15);
			// Centers the title and subtitle
			vBox.setAlignment(Pos.CENTER);
			borderPane.setAlignment(vBox, Pos.CENTER);

			// Runs the next stage when a keyboard/mouse button is pressed
			titleScene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					primaryStage.hide();
					backgroundVideoPlayer.stop();
					options();
				}
			});
			titleScene.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					primaryStage.hide();
					backgroundVideoPlayer.stop();
					options();
				}
			});

			// Attaches stylesheet
			titleScene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
			title.getStyleClass().add("title");
			start.getStyleClass().add("start");

			primaryStage.setScene(titleScene);
			primaryStage.setTitle("Main Screen");
			primaryStage.setResizable(false);
			primaryStage.setFullScreen(true);
			// Removes "Press ESC to exit fullscreen" tooltip
			primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
			primaryStage.show();
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	private void options() {
		InetAddress ip = null;
		String address = null;

		// Layout for error window
		VBox errorVBox = new VBox();
		BorderPane errorBorderPane = new BorderPane(errorVBox);
		Scene errorScene = new Scene(errorBorderPane, 300, 150);
		Stage errorStage = new Stage();
		errorStage.setScene(errorScene);
		errorStage.setTitle("Error");

		Text errorText = new Text("Undefined value entered");
		Button exitError = new Button("OK");

		// Exits error window when button is clicked
		exitError.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				errorStage.hide();
			}
		});

		errorVBox.setSpacing(15);
		errorVBox.getChildren().add(errorText);
		errorVBox.getChildren().add(exitError);

		errorVBox.setAlignment(Pos.CENTER);
		errorBorderPane.setAlignment(errorVBox, Pos.CENTER);

		// Registers host IP address and host username
		try {
			ip = ip.getLocalHost();
			address = ip.getHostAddress();
			userName = ip.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		// Layout for options window
		GridPane gridPane = new GridPane();

		Scene mainScene = new Scene(gridPane, 725, 400);
		Stage mainStage = new Stage();

		Text createServerText = new Text("Create Server:");
		Text serverIPAddressText = new Text("Machine IP Address:");
		Text serverIPAddress = new Text(address);
		Label serverPortLabel = new Label("Server Port:");
		TextField serverPortField = new TextField();
		serverPortField.setPrefWidth(125);
		serverPortField.setPromptText("0-65535");
		Button serverStart = new Button("Start Server");
		serverStart.setPrefWidth(125);

		Label userNameLabel = new Label("Username:");
		Label userNameLabel2 = new Label(userName);

		Button setUserNameButton = new Button("Set Username");
		setUserNameButton.setPrefWidth(125);

		GridPane userNameGridPane = new GridPane();
		userNameGridPane.setHgap(15);
		userNameGridPane.setPadding(new Insets(20));
		Scene userNameScene = new Scene(userNameGridPane);
		Stage userNameStage = new Stage();
		userNameStage.setScene(userNameScene);
		userNameStage.setTitle("Set Username");

		Label userNameServerLabel = new Label("Username: ");
		TextField serverUserNameField = new TextField();
		serverUserNameField.setText(userName);
		serverUserNameField.setPrefWidth(150);
		Button userNameApplyButton = new Button("Apply");

		// Sets the username variable to entered username when button is clicked
		userNameApplyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				userName = serverUserNameField.getText();
				userNameLabel2.setText(userName);
				// Hides username window
				userNameStage.hide();
				// Shows options window
				mainStage.show();
			}
		});

		// Triggers apply button action method if enter is pressed
		serverUserNameField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					userNameApplyButton.fire();
				}
			}
		});

		userNameGridPane.add(userNameServerLabel, 0, 0);
		userNameGridPane.add(serverUserNameField, 1, 0);
		userNameGridPane.add(userNameApplyButton, 3, 0);

		// Opens set username window
		setUserNameButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mainStage.hide();
				userNameStage.show();
			}
		});

		// Sets client chat color
		Label chatColorLabel = new Label("Chat color: ");
		ColorPicker chatColor = new ColorPicker();
		chatColor.setValue(Color.BLACK);
		chatColor.setPrefWidth(50);

		// Enable sound
		CheckBox enableSoundBox = new CheckBox("Enable Sound");
		// Default option is checked
		enableSoundBox.setSelected(true);

		// Creates server when button is clicked
		serverStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Sets variable equal to port
				serverPortString = serverPortField.getText();
				try {
					// Converts port to integer
					serverPortInt = Integer.parseInt(serverPortString);
					// Checks if a valid port is entered
					if (serverPortInt >= 0 && serverPortInt <= 65535) {
						// Hides options and error windows
						mainStage.hide();
						errorStage.hide();
						// Runs server method with port, username, chat color, sound
						serverChat(serverPortInt, userName, chatColor.getValue(), enableSoundBox.isSelected());
					} else {
						// Port is invalid
						serverPortField.clear();
						errorStage.show();
					}
				} // Cannot convert to integer
				catch (Exception e) {
					serverPortField.clear();
					errorStage.show();
					e.printStackTrace();
				}
			}
		});

		// Triggers serverStart button action when enter is pressed in textfield
		serverPortField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					serverStart.fire();
				}
			}
		});

		Text joinServerText = new Text("Join Server:");

		Label joinIPAddressLabel = new Label("Server IP Address:");
		TextField joinIPAddressField = new TextField();
		joinIPAddressField.setPrefWidth(125);
		joinIPAddressField.setPromptText("E.g. 127.0.0.1");
		Label joinPortLabel = new Label("Server Port:");
		TextField joinPortField = new TextField();
		joinPortField.setPrefWidth(125);
		joinPortField.setPromptText("0-65535");
		Button joinStart = new Button("Join Server");
		joinStart.setPrefWidth(125);

		// Runs client method when button is clicked
		joinStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// Retrieves server IP Address and server port
				joinIPAddress = joinIPAddressField.getText();
				joinPortString = joinPortField.getText();
				try {
					// Converts port to integer
					joinPortInt = Integer.parseInt(joinPortString);
					// Checks if port is valid
					if (joinPortInt >= 0 && joinPortInt <= 65535) {
						mainStage.hide();
						errorStage.hide();
						// Runs client method with server IP Address, username, chat color, sound
						clientChat(joinIPAddress, joinPortInt, userName, chatColor.getValue(),
								enableSoundBox.isSelected());
					} // If port is invalid
					else {
						joinIPAddressField.clear();
						joinPortField.clear();
						errorStage.show();
					}
				} // If port cannot be converted to integer
				catch (Exception e) {
					joinIPAddressField.clear();
					joinPortField.clear();
					errorStage.show();
				}
			}
		});

		joinPortField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					joinStart.fire();
				}
			}
		});

		// Layout
		gridPane.add(createServerText, 0, 0);
		gridPane.add(serverIPAddressText, 0, 1);
		gridPane.add(serverIPAddress, 1, 1);
		gridPane.add(serverPortLabel, 0, 2);
		gridPane.add(serverPortField, 1, 2);
		gridPane.add(serverStart, 1, 4);
		gridPane.add(joinServerText, 2, 0);
		gridPane.add(joinIPAddressLabel, 2, 1);
		gridPane.add(joinIPAddressField, 3, 1);
		gridPane.add(joinPortLabel, 2, 2);
		gridPane.add(joinPortField, 3, 2);
		gridPane.add(joinStart, 3, 4);
		gridPane.add(userNameLabel, 0, 6);
		gridPane.add(userNameLabel2, 1, 6);
		gridPane.add(setUserNameButton, 2, 6);
		gridPane.add(chatColorLabel, 0, 7);
		gridPane.add(chatColor, 1, 7);
		gridPane.add(enableSoundBox, 2, 7);
		gridPane.setPadding(new Insets(30, 10, 10, 25));
		gridPane.setVgap(25);
		gridPane.setHgap(50);

		// Removes focus from textfields
		gridPane.requestFocus();

		mainStage.setX(primaryScreenBounds.getWidth() / 3.5);
		mainStage.setY(primaryScreenBounds.getHeight() / 3.5);
		mainStage.setScene(mainScene);
		mainStage.setTitle("Options");
		mainStage.setResizable(false);
		mainStage.show();
	}

	@SuppressWarnings("static-access")
	public void serverChat(int port, String userName, Color chatColor, Boolean enableSound)
			throws FileNotFoundException {
		InetAddress ip = null;
		String address = null;

		// Retrieves host IP address
		try {
			ip = ip.getLocalHost();
			address = ip.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		// Layout
		VBox serverMessageVBox = new VBox();
		VBox messageBoardVBox = new VBox();
		BorderPane messageBoardBorderPane = new BorderPane();
		messageBoardBorderPane.getChildren().addAll(messageBoardVBox, serverMessageVBox);
		messageBoardVBox.setAlignment(Pos.TOP_LEFT);
		serverMessageVBox.setAlignment(Pos.TOP_LEFT);
		messageBoardBorderPane.setAlignment(serverMessageVBox, Pos.TOP_LEFT);
		messageBoardBorderPane.setAlignment(messageBoardVBox, Pos.TOP_LEFT);

		HBox chatHBox = new HBox();

		Stage chatStage = new Stage();
		chatStage.initStyle(StageStyle.UNDECORATED);
		chatStage.setTitle("Chat");
		chatStage.setResizable(false);
		chatStage.setAlwaysOnTop(true);
		chatStage.setX(0);
		chatStage.setY(primaryScreenBounds.getMaxY() - 50);
		chatStage.show();

		Scene messageBoardScene = new Scene(messageBoardBorderPane);
		Stage messageBoardStage = new Stage();

		// Attaches stylesheet
		messageBoardScene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

		TextField chatField = new TextField();
		chatField.setPrefSize(primaryScreenBounds.getMaxX() - 300, 50);

		Button sendMessage = new Button("Send");
		sendMessage.setPrefHeight(50);

		// Runs send message method when button is clicked
		sendMessage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sendMessage(chatField.getText(), serverClientSocket, messageBoardVBox, userName, chatColor,
						enableSound);
				chatField.clear();
			}
		});

		// Runs sendMessage button action when enter button is pressed
		chatField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					sendMessage.fire();
				}
			}
		});

		Button receiveMessage = new Button("Receive");
		receiveMessage.setPrefHeight(50);

		// Runs receiveMessage method when button is clicked
		receiveMessage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				receiveMessage(serverClientSocket, messageBoardVBox, enableSound);
			}
		});

		Button clearChat = new Button("Clear");
		clearChat.setPrefHeight(50);

		clearChat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clearChat(messageBoardVBox);
				messageBoardVBox.getChildren().add(new Text("\n\n\n\n\n"));
			}
		});

		ImageView sendEmojiImage = new ImageView(new Image(new FileInputStream("res/send_emoji.png")));
		sendEmojiImage.setPreserveRatio(true);
		sendEmojiImage.setFitWidth(40);
		sendEmojiImage.setFitHeight(40);
		ToggleButton sendEmoji = new ToggleButton();
		sendEmoji.setGraphic(sendEmojiImage);
		sendEmoji.setPrefWidth(40);
		sendEmoji.setPrefHeight(40);

		// Layout
		chatHBox.getChildren().addAll(sendEmoji, chatField, sendMessage, receiveMessage, clearChat);
		chatHBox.setSpacing(10);

		Scene chatScene = new Scene(chatHBox);

		chatScene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

		chatStage.setScene(chatScene);

		ImageView BibleThumpIcon = new ImageView(new Image(new FileInputStream("res/Emotes/BibleThump.png")));
		BibleThumpIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "BibleThump";
				chatField.setText(chatMessage);
				chatField.requestFocus();
			}
		});
		ImageView ClapIcon = new ImageView(new Image(new FileInputStream("res/Emotes/Clap.gif")));
		ClapIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "Clap";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView EZIcon = new ImageView(new Image(new FileInputStream("res/Emotes/EZ.png")));
		EZIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "EZ";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView FrankerZIcon = new ImageView(new Image(new FileInputStream("res/Emotes/FrankerZ.png")));
		FrankerZIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "FrankerZ";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView lessThanThreeIcon = new ImageView(new Image(new FileInputStream("res/Emotes/less_than_three.png")));
		lessThanThreeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "<3";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView LULIcon = new ImageView(new Image(new FileInputStream("res/Emotes/LUL.png")));
		LULIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "LUL";
				chatField.setText(chatMessage);
				chatField.requestFocus();
			}
		});
		ImageView monkaSIcon = new ImageView(new Image(new FileInputStream("res/Emotes/monkaS.png")));
		monkaSIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "monkaS";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView monkaXIcon = new ImageView(new Image(new FileInputStream("res/Emotes/monkaX.gif")));
		monkaXIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "monkaX";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView PepeHandsIcon = new ImageView(new Image(new FileInputStream("res/Emotes/PepeHands.png")));
		PepeHandsIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "PepeHands";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView RareBoiIcon = new ImageView(new Image(new FileInputStream("res/Emotes/RareBoi.gif")));
		RareBoiIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "RareBoi";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView RareParrotIcon = new ImageView(new Image(new FileInputStream("res/Emotes/RareParrot.gif")));
		RareParrotIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "RareParrot";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView SeemsGoodIcon = new ImageView(new Image(new FileInputStream("res/Emotes/SeemsGood.png")));
		SeemsGoodIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "SeemsGood";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView smileyFaceIcon = new ImageView(new Image(new FileInputStream("res/Emotes/smiley_face.png")));
		smileyFaceIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += ":)";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView VoteNayIcon = new ImageView(new Image(new FileInputStream("res/Emotes/VoteNay.png")));
		VoteNayIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "VoteNay";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView VoteYayIcon = new ImageView(new Image(new FileInputStream("res/Emotes/VoteYay.png")));
		VoteYayIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "VoteYay";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView WutFaceIcon = new ImageView(new Image(new FileInputStream("res/Emotes/WutFace.png")));
		WutFaceIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "WutFace";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});

		GridPane emojiSelectorPane = new GridPane();
		// emojiSelectorPane.getStyleClass().add("emojiSelectorPane");
		emojiSelectorPane.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: blue;");
		emojiSelectorPane.setHgap(10);
		emojiSelectorPane.setVgap(10);
		Scene emojiSelectorScene = new Scene(emojiSelectorPane);
		Stage emojiSelectorStage = new Stage();
		emojiSelectorStage.setScene(emojiSelectorScene);
		emojiSelectorStage.initStyle(StageStyle.UNDECORATED);
		emojiSelectorStage.setX(chatStage.getX());
		emojiSelectorStage.setY(chatStage.getY() - 150);

		emojiSelectorPane.add(smileyFaceIcon, 0, 0);
		emojiSelectorPane.add(LULIcon, 1, 0);
		emojiSelectorPane.add(lessThanThreeIcon, 2, 0);
		emojiSelectorPane.add(EZIcon, 3, 0);
		emojiSelectorPane.add(SeemsGoodIcon, 4, 0);
		emojiSelectorPane.add(monkaSIcon, 0, 1);
		emojiSelectorPane.add(WutFaceIcon, 1, 1);
		emojiSelectorPane.add(monkaXIcon, 2, 1);
		emojiSelectorPane.add(PepeHandsIcon, 3, 1);
		emojiSelectorPane.add(BibleThumpIcon, 4, 1);
		emojiSelectorPane.add(RareBoiIcon, 0, 2);
		emojiSelectorPane.add(RareParrotIcon, 1, 2);
		emojiSelectorPane.add(VoteYayIcon, 2, 2);
		emojiSelectorPane.add(VoteNayIcon, 3, 2);
		emojiSelectorPane.add(ClapIcon, 4, 2);

		sendEmoji.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			if (emojiSelectorStage.isShowing()) {
				emojiSelectorStage.hide();
			} else {
				emojiSelectorStage.show();
			}
		}));

		Text connectText = new Text(
				"Server IP Address: " + address + "\nInitiated with port: " + port + "\nWaiting for connection...");
		connectText.getStyleClass().add("message");

		serverMessageVBox.getChildren().add(connectText);

		messageBoardVBox.getChildren().add(new Text("\n\n\n\n\n"));

		messageBoardStage.setTitle("Message Board");
		messageBoardStage.setScene(messageBoardScene);
		messageBoardStage.setMaximized(true);
		messageBoardStage.setResizable(false);
		messageBoardStage.show();

		// Initiates server
		initiateServer(port, serverMessageVBox, enableSound);

		// Exits program when either window is closed
		EventHandler<WindowEvent> terminate = new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
			};
		};

		chatStage.setOnCloseRequest(terminate);
		messageBoardStage.setOnCloseRequest(terminate);

	}

	@SuppressWarnings("static-access")
	public void clientChat(String serverIPAddress, int serverPort, String userName, Color chatColor,
			Boolean enableSound) throws FileNotFoundException {
		// Layout
		VBox clientMessageVBox = new VBox();
		VBox messageBoardVBox = new VBox();
		BorderPane messageBoardBorderPane = new BorderPane();
		messageBoardBorderPane.getChildren().addAll(clientMessageVBox, messageBoardVBox);

		messageBoardVBox.setAlignment(Pos.TOP_LEFT);
		clientMessageVBox.setAlignment(Pos.TOP_LEFT);
		messageBoardBorderPane.setAlignment(clientMessageVBox, Pos.TOP_LEFT);
		messageBoardBorderPane.setAlignment(messageBoardVBox, Pos.TOP_LEFT);

		HBox chatHBox = new HBox();

		Stage chatStage = new Stage();
		chatStage.initStyle(StageStyle.UNDECORATED);
		chatStage.setTitle("Chat");
		chatStage.setResizable(false);
		chatStage.setAlwaysOnTop(true);
		chatStage.setX(0);
		chatStage.setY(primaryScreenBounds.getMaxY() - 50);
		chatStage.show();

		Scene messageBoardScene = new Scene(messageBoardBorderPane);
		Stage messageBoardStage = new Stage();

		// Attaches Stylesheet
		messageBoardScene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());

		TextField chatField = new TextField();
		chatField.setPrefSize(primaryScreenBounds.getMaxX() - 300, 50);

		Button sendMessage = new Button("Send");
		sendMessage.setPrefHeight(50);

		// Runs send message method when button is clicked
		sendMessage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				sendMessage(chatField.getText(), client, messageBoardVBox, userName, chatColor, enableSound);
				chatField.clear();
			}
		});

		// Runs sendMessage button action when enter button is pressed
		chatField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					sendMessage.fire();
				}
			}
		});

		Button receiveMessage = new Button("Receive");
		receiveMessage.setPrefHeight(50);

		// Runs receiveMessage method when button is clicked
		receiveMessage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				receiveMessage(client, messageBoardVBox, enableSound);
			}
		});

		Button clearChat = new Button("Clear");
		clearChat.setPrefHeight(50);

		clearChat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clearChat(messageBoardVBox);
				messageBoardVBox.getChildren().add(new Text("\n\n\n\n\n"));
			}
		});

		ImageView sendEmojiImage = new ImageView(new Image(new FileInputStream("res/send_emoji.png")));
		sendEmojiImage.setPreserveRatio(true);
		sendEmojiImage.setFitWidth(40);
		sendEmojiImage.setFitHeight(40);
		ToggleButton sendEmoji = new ToggleButton();
		sendEmoji.setGraphic(sendEmojiImage);
		sendEmoji.setPrefWidth(40);
		sendEmoji.setPrefHeight(40);

		// Layout
		chatHBox.getChildren().addAll(sendEmoji, chatField, sendMessage, receiveMessage, clearChat);
		chatHBox.setSpacing(10);

		Scene chatScene = new Scene(chatHBox);
		chatScene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
		chatStage.setScene(chatScene);

		ImageView BibleThumpIcon = new ImageView(new Image(new FileInputStream("res/Emotes/BibleThump.png")));

		BibleThumpIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "BibleThump";
				chatField.setText(chatMessage);
				chatField.requestFocus();
			}
		});
		ImageView ClapIcon = new ImageView(new Image(new FileInputStream("res/Emotes/Clap.gif")));
		ClapIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "Clap";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView EZIcon = new ImageView(new Image(new FileInputStream("res/Emotes/EZ.png")));
		EZIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "EZ";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView FrankerZIcon = new ImageView(new Image(new FileInputStream("res/Emotes/FrankerZ.png")));
		FrankerZIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "FrankerZ";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView lessThanThreeIcon = new ImageView(new Image(new FileInputStream("res/Emotes/less_than_three.png")));
		lessThanThreeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "<3";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView LULIcon = new ImageView(new Image(new FileInputStream("res/Emotes/LUL.png")));
		LULIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "LUL";
				chatField.setText(chatMessage);
				chatField.requestFocus();
			}
		});
		ImageView monkaSIcon = new ImageView(new Image(new FileInputStream("res/Emotes/monkaS.png")));
		monkaSIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "monkaS";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView monkaXIcon = new ImageView(new Image(new FileInputStream("res/Emotes/monkaX.gif")));
		monkaXIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "monkaX";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView PepeHandsIcon = new ImageView(new Image(new FileInputStream("res/Emotes/PepeHands.png")));
		PepeHandsIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "PepeHands";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView RareBoiIcon = new ImageView(new Image(new FileInputStream("res/Emotes/RareBoi.gif")));
		RareBoiIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "RareBoi";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView RareParrotIcon = new ImageView(new Image(new FileInputStream("res/Emotes/RareParrot.gif")));
		RareParrotIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "RareParrot";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView SeemsGoodIcon = new ImageView(new Image(new FileInputStream("res/Emotes/SeemsGood.png")));
		SeemsGoodIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "SeemsGood";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView smileyFaceIcon = new ImageView(new Image(new FileInputStream("res/Emotes/smiley_face.png")));
		smileyFaceIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += ":)";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView VoteNayIcon = new ImageView(new Image(new FileInputStream("res/Emotes/VoteNay.png")));
		VoteNayIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "VoteNay";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView VoteYayIcon = new ImageView(new Image(new FileInputStream("res/Emotes/VoteYay.png")));
		VoteYayIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "VoteYay";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});
		ImageView WutFaceIcon = new ImageView(new Image(new FileInputStream("res/Emotes/WutFace.png")));
		WutFaceIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				String chatMessage = chatField.getText();
				chatMessage += "WutFace";
				chatField.setText(chatMessage);
				chatField.requestFocus();

			}
		});

		GridPane emojiSelectorPane = new GridPane();
		// emojiSelectorPane.getStyleClass().add("emojiSelectorPane");
		emojiSelectorPane.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: blue;");
		emojiSelectorPane.setHgap(10);
		emojiSelectorPane.setVgap(10);
		Scene emojiSelectorScene = new Scene(emojiSelectorPane);
		Stage emojiSelectorStage = new Stage();
		emojiSelectorStage.setScene(emojiSelectorScene);
		emojiSelectorStage.initStyle(StageStyle.UNDECORATED);
		emojiSelectorStage.setX(chatStage.getX());
		emojiSelectorStage.setY(chatStage.getY() - 150);

		emojiSelectorPane.add(smileyFaceIcon, 0, 0);
		emojiSelectorPane.add(LULIcon, 1, 0);
		emojiSelectorPane.add(lessThanThreeIcon, 2, 0);
		emojiSelectorPane.add(EZIcon, 3, 0);
		emojiSelectorPane.add(SeemsGoodIcon, 4, 0);
		emojiSelectorPane.add(monkaSIcon, 0, 1);
		emojiSelectorPane.add(WutFaceIcon, 1, 1);
		emojiSelectorPane.add(monkaXIcon, 2, 1);
		emojiSelectorPane.add(PepeHandsIcon, 3, 1);
		emojiSelectorPane.add(BibleThumpIcon, 4, 1);
		emojiSelectorPane.add(RareBoiIcon, 0, 2);
		emojiSelectorPane.add(RareParrotIcon, 1, 2);
		emojiSelectorPane.add(VoteYayIcon, 2, 2);
		emojiSelectorPane.add(VoteNayIcon, 3, 2);
		emojiSelectorPane.add(ClapIcon, 4, 2);

		sendEmoji.selectedProperty().addListener(((observable, oldValue, newValue) -> {
			if (emojiSelectorStage.isShowing()) {
				emojiSelectorStage.hide();
			} else {
				emojiSelectorStage.show();
			}
		}));

		Text connectText = new Text("Connecting to " + serverIPAddress + " with port: " + serverPort);
		connectText.getStyleClass().add("message");

		clientMessageVBox.getChildren().add(connectText);

		messageBoardVBox.getChildren().add(new Text("\n\n\n"));

		messageBoardStage.setTitle("Message Board");
		messageBoardStage.setScene(messageBoardScene);
		messageBoardStage.setMaximized(true);
		messageBoardStage.setResizable(false);
		messageBoardStage.show();

		// Runs initiate client method
		initiateClient(serverIPAddress, serverPort, clientMessageVBox, enableSound);

		// Exits program when either window is closed
		EventHandler<WindowEvent> terminate = new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
			};
		};

		chatStage.setOnCloseRequest(terminate);
		messageBoardStage.setOnCloseRequest(terminate);

	}

	/**
	 * Creates a server and accepts connection
	 * 
	 * @param port
	 *            Server port from 0 to 65535
	 * @param messageBoardVBox
	 *            Message board to display messages to user
	 * @param soundEnabled
	 *            Whether sound is enabled
	 */
	private void initiateServer(int port, VBox serverMessageVBox, Boolean soundEnabled) {
		try {
			// Creates a server with entered port
			serverSocket = new ServerSocket(port);
			// Accepts connection from client (Blocking method)
			serverClientSocket = serverSocket.accept();

			// Displays success message
			Text connectionSuccessfulText = new Text(
					"Successfully connected to " + serverClientSocket.getInetAddress());
			connectionSuccessfulText.getStyleClass().add("message");
			serverMessageVBox.getChildren().add(connectionSuccessfulText);

			if (soundEnabled) {
				Media connectionEstablishedSound = new Media(
						new File("res/Sounds/connection_established.mp3").toURI().toString());
				MediaPlayer connectionEstablishedPlayer = new MediaPlayer(connectionEstablishedSound);
				connectionEstablishedPlayer.play();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connects client to server
	 * 
	 * @param serverIPAddress
	 * @param serverPort
	 * @param messageBoardVBox
	 *            Displays messages to user
	 * @param soundEnabled
	 */
	private void initiateClient(String serverIPAddress, int serverPort, VBox clientMessageVBox, Boolean soundEnabled) {
		try {
			client = new Socket(serverIPAddress, serverPort);
			Text connectionSuccessfulText = new Text("Connected.");
			connectionSuccessfulText.getStyleClass().add("message");
			clientMessageVBox.getChildren().add(connectionSuccessfulText);

			if (soundEnabled) {
				Media connectionEstablishedSound = new Media(
						new File("res/Sounds/connection_established.mp3").toURI().toString());
				MediaPlayer connectionEstablishedPlayer = new MediaPlayer(connectionEstablishedSound);
				connectionEstablishedPlayer.play();
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends message to server/client and displays message to message board
	 * 
	 * @param message
	 *            Message to be sent
	 * @param socket
	 *            Server/Client socket
	 * @param messageBoardVBox
	 *            Message board to display messages
	 * @param userName
	 *            Username for modifying message
	 * @param chatColor
	 *            Color of server/client
	 * @param soundEnabled
	 *            Whether sound is enabled
	 */
	private void sendMessage(String message, Socket socket, VBox messageBoardVBox, String userName, Color chatColor,
			Boolean soundEnabled) {
		try {
			// Allows messages to be sent to server/client
			dos = new DataOutputStream(socket.getOutputStream());
			// Modifies message to display current time and username
			message = ">>> " + time() + " " + userName + ": " + message;
			if (messageContainsEmoticon(message, emoticonArray)) {
				String messageWithEmoticon = message;
				HBox messageBoardHBox = new HBox();
				while (messageContainsEmoticon(messageWithEmoticon, emoticonArray)) {
					messageWithEmoticon = insertEmoticon(messageWithEmoticon, messageBoardHBox, messageBoardVBox,
							chatColor);
				}
				messageBoardVBox.getChildren().add(messageBoardHBox);
			} else {

				// Adds message to personal message board
				Text messageText = new Text(message);
				messageText.getStyleClass().add("message");
				messageText.setFill(chatColor);
				messageBoardVBox.getChildren().add(messageText);
			}
			// Sends message to server/client
			dos.writeUTF(message);
			dos.flush();
			// Plays sound
			if (soundEnabled) {
				Media sendMessageSound = new Media(new File("res/Sounds/send_message.mp3").toURI().toString());
				MediaPlayer sendMessagePlayer = new MediaPlayer(sendMessageSound);
				sendMessagePlayer.play();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param socket
	 *            Server/Client socket
	 * @param messageBoardVBox
	 *            Message board to display messages
	 * @param soundEnabled
	 *            Whether sound is enabled
	 */
	private void receiveMessage(Socket socket, VBox messageBoardVBox, Boolean soundEnabled) {
		try {
			// Allows messages to be received from server/client
			dis = new DataInputStream(socket.getInputStream());
			// Receives message as string
			String received = dis.readUTF();
			if (messageContainsEmoticon(received, emoticonArray)) {
				String messageWithEmoticon = received;
				HBox messageBoardHBox = new HBox();
				while (messageContainsEmoticon(messageWithEmoticon, emoticonArray)) {
					messageWithEmoticon = insertEmoticon(messageWithEmoticon, messageBoardHBox, messageBoardVBox,
							Color.BLACK);
				}
				messageBoardVBox.getChildren().add(messageBoardHBox);
			} else {
				// Adds message to message board
				Text messageText = new Text(received);
				messageText.getStyleClass().add("message");
				messageBoardVBox.getChildren().add(messageText);
			}
			// Plays sound
			if (soundEnabled) {
				Media receiveMessageSound = new Media(new File("res/Sounds/receive_message.mp3").toURI().toString());
				MediaPlayer receiveMessagePlayer = new MediaPlayer(receiveMessageSound);
				receiveMessagePlayer.play();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String whichEmoticon(String message, String[] emoticonArray) {
		return Arrays.stream(emoticonArray).filter(message::contains).findAny().toString();
	}

	private Boolean messageContainsEmoticon(String message, String[] emoticonArray) {
		return Arrays.stream(emoticonArray).parallel().anyMatch(message::contains);
	}

	private String insertEmoticon(String message, HBox messageBoardHBox, VBox messageBoardVBox, Color chatColor) {
		int index = 0;
		String s1 = null;
		String s2 = null;
		ImageView emoticon = null;
		try {
			if (whichEmoticon(message, emoticonArray).contains(":)")) {
				index = message.indexOf(":)");
				s2 = message.substring(index + ":)".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/smiley_face.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("LUL")) {
				index = message.indexOf("LUL");
				s2 = message.substring(index + "LUL".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/LUL.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("<3")) {
				index = message.indexOf("<3");
				s2 = message.substring(index + "<3".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/less_than_three.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("EZ")) {
				index = message.indexOf("EZ");
				s2 = message.substring(index + "EZ".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/EZ.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("SeemsGood")) {
				index = message.indexOf("SeemsGood");
				s2 = message.substring(index + "SeemsGood".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/SeemsGood.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("monkaS")) {
				index = message.indexOf("monkaS");
				s2 = message.substring(index + "monkaS".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/monkaS.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("WutFace")) {
				index = message.indexOf("WutFace");
				s2 = message.substring(index + "WutFace".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/WutFace.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("monkaX")) {
				index = message.indexOf("monkaX");
				s2 = message.substring(index + "monkaX".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/monkaX.gif")));
			} else if (whichEmoticon(message, emoticonArray).contains("PepeHands")) {
				index = message.indexOf("PepeHands");
				s2 = message.substring(index + "PepeHands".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/PepeHands.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("BibleThump")) {
				index = message.indexOf("BibleThump");
				s2 = message.substring(index + "BibleThump".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/BibleThump.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("RareBoi")) {
				index = message.indexOf("RareBoi");
				s2 = message.substring(index + "RareBoi".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/RareBoi.gif")));
			} else if (whichEmoticon(message, emoticonArray).contains("RareParrot")) {
				index = message.indexOf("RareParrot");
				s2 = message.substring(index + "RareParrot".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/RareParrot.gif")));
			} else if (whichEmoticon(message, emoticonArray).contains("VoteYay")) {
				index = message.indexOf("VoteYay");
				s2 = message.substring(index + "VoteYay".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/VoteYay.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("VoteNay")) {
				index = message.indexOf("VoteNay");
				s2 = message.substring(index + "VoteNay".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/VoteNay.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("FrankerZ")) {
				index = message.indexOf("FrankerZ");
				s2 = message.substring(index + "FrankerZ".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/FrankerZ.png")));
			} else if (whichEmoticon(message, emoticonArray).contains("Clap")) {
				index = message.indexOf("Clap");
				s2 = message.substring(index + "Clap".length());
				emoticon = new ImageView(new Image(new FileInputStream("res/Emotes/Clap.gif")));
			}

			s1 = message.substring(0, index);

			Text messagePart1 = new Text(s1);
			Text messagePart2 = new Text(s2);

			messagePart1.getStyleClass().add("message");
			messagePart1.setFill(chatColor);
			messagePart2.getStyleClass().add("message");
			messagePart2.setFill(chatColor);

			messageBoardHBox.getChildren().add(messagePart1);
			messageBoardHBox.getChildren().add(emoticon);
			if (messageContainsEmoticon(s2, emoticonArray) == false) {
				messageBoardHBox.getChildren().add(messagePart2);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return s2;
	}

	private void clearChat(VBox messageBoardVBox) {
		messageBoardVBox.getChildren().clear();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
