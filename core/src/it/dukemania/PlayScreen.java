package it.dukemania;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import it.dukemania.View.notesGraphics.Columns;
import it.dukemania.View.notesGraphics.ComputingShift;
import it.dukemania.View.notesGraphics.ComputingShiftImpl;
import it.dukemania.View.notesGraphics.EventsFromKeyboard;
import it.dukemania.View.notesGraphics.EventsFromKeyboardImpl;
import it.dukemania.View.notesGraphics.Key;
import it.dukemania.View.notesGraphics.KeyImpl;
import it.dukemania.View.notesGraphics.Note;
import it.dukemania.View.notesGraphics.NoteImpl;
import it.dukemania.View.notesGraphics.NoteLogic;
import it.dukemania.View.notesGraphics.NoteLogicImpl;
import it.dukemania.View.notesGraphics.Pair;
import it.dukemania.View.notesGraphics.Size;
import it.dukemania.View.notesGraphics.SizeImpl;


public class PlayScreen extends ApplicationAdapter {
    //Engine ae = null;
    //cambiare la texture della scoreboard e il testo

    //sofi
    //private Logic logic = new LogicImpl();         //rapo
    private final Size dimensions;
    private Pair<Integer, Integer> scoreboardSize;
    private Stage buttonsStage;
    private Stage stage;
    private TextureAtlas atlas;
    private Skin skin;
    private BitmapFont font;
    private BitmapFont fontScoreboard;
    private GlyphLayout layout = new GlyphLayout(); 
    private float fontWidth;
    private float fontHeight;
    private FreeTypeFontGenerator generator;
    private FreeTypeFontParameter parameter;
    private String text;
    private TextButtonStyle styleDown;
    private TextButtonStyle styleUp;
    private Texture background;
    private Texture scoreboard;
    private Texture textureNote;
    private Texture textureSparks;
    private Image backgroundImage;
    private int buttonHeight;
    private SpriteBatch batch;
    private SpriteBatch backgroundBatch;
    private int xNote;
    private int yNote;
    private int posySparks;
    private int posyBlue;
    private NoteLogic note6uno; //nell'attesa dell'array di rapo
    private NoteLogic note7uno;
    private NoteLogic note8uno;
    private NoteLogic note9uno;
    private NoteLogic note10uno;
    private List<Note> notes = new ArrayList<>(); 
    private List<NoteLogic> logicNotes = new ArrayList<>(); //per ora vuoto, poi array di rapo
    private List<Note> notesPlaying = new ArrayList<>(); 
    private long timeStart = 0;
    private EventsFromKeyboard keyboard;
    private Key key;
    private ComputingShift shift = new ComputingShiftImpl();
    private OrthographicCamera camera = new OrthographicCamera();
    private Viewport buttonsViewport;
    private Viewport stageViewport;
    private final int numberOfColumns;
    private float deltaTime = 0;
    private List<TextButton> buttons = new ArrayList<>();
    private static final int BUTTONDIM = 120;

        public PlayScreen() { 
            this.xNote = 50;
            this.yNote = 80;
            this.dimensions = new SizeImpl();
            this.numberOfColumns = this.dimensions.getNumberOfColumns();
            this.buttonHeight = this.BUTTONDIM;
            this.posySparks = this.buttonHeight - 35; //15
            this.posyBlue = this.buttonHeight;   //20
            //notes = logic.getnotes();      //rapo
    }
	


	@Override
	public void create() {
	    this.background = new Texture(Gdx.files.internal("Textures/blueBackground.png")); 
	    this.backgroundImage = new Image(this.background);
	    this.textureNote = new Texture(Gdx.files.internal("Textures/blueNote.png"));
	    this.textureSparks = new Texture(Gdx.files.internal("Textures/blueSpark.png"));
	    this.scoreboard = new Texture(Gdx.files.internal("Textures/scoreboard.png"), true);
	    this.scoreboard.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	    this.batch = new SpriteBatch();
	    this.backgroundBatch = new SpriteBatch();
	    this.buttonsViewport = new ExtendViewport(this.dimensions.getSize().getX(), this.dimensions.getSize().getY(), camera);
	    this.stageViewport = new StretchViewport(this.dimensions.getSize().getX(), this.dimensions.getSize().getY(), camera); //backgroundimage.width
	    //ae = new Engine();

	    this.buttonsStage = new Stage(this.buttonsViewport, this.batch);
	    this.stage = new Stage(this.stageViewport, this.backgroundBatch);
        Gdx.input.setInputProcessor(buttonsStage);

        this.note6uno = new NoteLogicImpl(1, 200, 1, Columns.COLUMN1, 200);
        this.note7uno = new NoteLogicImpl(2, 600, 2, Columns.COLUMN2, 200);
        this.note8uno = new NoteLogicImpl(1, 1000, 3, Columns.COLUMN3, 200);
        this.note9uno = new NoteLogicImpl(1, 1400, 4, Columns.COLUMN4, 200);
        this.note10uno = new NoteLogicImpl(2, 1600, 5, Columns.COLUMN2, 200);

        this.logicNotes.add(note6uno);
        this.logicNotes.add(note7uno);
        this.logicNotes.add(note8uno);
        this.logicNotes.add(note9uno); 
        this.logicNotes.add(note10uno);

        this.font = new BitmapFont();
        this.skin = new Skin();
        this.atlas = new TextureAtlas(Gdx.files.internal("pinkAndBlueButtons.atlas"));
        this.skin.addRegions(atlas);
        this.text = "700000";

        this.generator = new FreeTypeFontGenerator(Gdx.files.internal("scoreboard_font.TTF"));
        this.parameter = new FreeTypeFontParameter();
        this.parameter.size = 40;
        this.parameter.color = Color.WHITE;
        this.parameter.shadowColor = Color.BLACK;
        this.parameter.shadowOffsetX = 2;
        this.fontScoreboard = generator.generateFont(this.parameter);
        this.fontScoreboard.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        this.styleDown = new TextButtonStyle();
        this.styleDown.font = font;
        this.styleDown.up = this.skin.getDrawable("buttonDown");
        this.styleDown.down = this.skin.getDrawable("buttonUp");
        this.styleUp = new TextButtonStyle();
        this.styleUp.font = font;
        this.styleUp.up = this.skin.getDrawable("buttonUp");
        this.styleUp.down = this.skin.getDrawable("buttonDown");

        //placement of the buttons 
        for (int i = 0; i < this.numberOfColumns; i++) {
            this.buttons.add(new TextButton("", this.styleUp));
            this.buttons.get(i).setSize(this.BUTTONDIM, this.BUTTONDIM);  //set the size of the buttons
            this.buttons.get(i).setPosition(i * this.dimensions.getSize().getX() / this.numberOfColumns + this.shift.calculateShifting(this.numberOfColumns) * i, 0);  //set the position of each button
            //this.buttons.get(i).setTransform(true);
            //this.buttons.get(i).setScale(0.6f);
            this.buttonsStage.addActor(this.buttons.get(i));
        }

        for (final NoteLogic noteLogic : logicNotes) {
            this.notes.add(associationNote(noteLogic));
        }

        //adding elements on the stage
        this.stage.addActor(backgroundImage);

	}
	
	
	//this method associates the logical note to the corresponding graphic note
	private Note associationNote(final NoteLogic noteLogic) {
            return new NoteImpl(this.dimensions.getSize().getY(), noteLogic.getColumn(), posyBlue, posySparks, noteLogic.getHeight() * this.yNote, noteLogic.getTimeStart(), noteLogic.getDuration(), this.numberOfColumns);
        }
	
	//this method returns the notes that are playing right now
	private List<Note> getPlayingNotes(final long actualTime) {
	    final List<Note> playing = new ArrayList<>();
            for (final Note n : this.notes) {
                if (n.getStartTime() <= actualTime) {
                    playing.add(n);
                }
            }
	    return playing;

	}
	
	//this method returns the notes that have already finished playing
	private List<Note> notFinished(final List<Note> playing, final long actualTime) {
	    final List<Note> finished = new ArrayList<>();
	    for (final Note n : playing) {
	        if (n.getStartTime() + n.getDuration() * (long) Math.pow(10, 3) >= actualTime) {
	            finished.add(n);
	        }
	    }
            return finished;

	}
	

	private void drawNote(final int posxNote, final int posyNote) {
	    final Rectangle clipBounds = new Rectangle(0, dimensions.getSize().getY() - 610, dimensions.getSize().getX(), dimensions.getSize().getY() - 190); //magic numbers

        this.batch.flush();
        ScissorStack.pushScissors(clipBounds);
        this.batch.draw(this.textureNote, posxNote, posyNote, this.xNote, this.yNote, 0, 1, 1, 0);
        this.batch.flush();
        ScissorStack.popScissors();

        /*renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(Color.RED);
        renderer.rect(clipBoundsprova.x, clipBoundsprova.y, clipBoundsprova.width, clipBoundsprova.height);
        renderer.circle(30, clipBounds.y, 3);
        renderer.end();*/

	}
	
	private void isSparked(final Note n) {
	    if (n.getPosyNote() <= this.posyBlue && n.getPosyNote() >= this.posyBlue - 40) {
            this.batch.draw(this.textureSparks, n.getPosxSpark(), this.posySparks, n.getxSpark(), n.getySpark(), 0, 1, 1, 0);
        }
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//ae.playBuffer();
		if (this.timeStart == 0) {
		    this.timeStart = Instant.now().toEpochMilli();
		}
		this.stage.draw();
		this.backgroundBatch.begin();
		this.buttonsStage.draw();
		this.batch.begin();


		layout.setText(fontScoreboard, this.text);
		this.fontWidth = layout.width;
		this.fontHeight = layout.height; 
		this.fontScoreboard.draw(batch, text, this.dimensions.getSize().getX() / 2 - this.fontWidth / 2, this.dimensions.getSize().getY() - this.fontHeight * 0.75f);
		this.batch.draw(this.scoreboard, 0, this.dimensions.getSize().getY() - 80, this.dimensions.getSize().getX(), 80);

		//set the style of the buttons
		for (final TextButton b : this.buttons) {
		    b.setStyle(this.styleUp);
		}

        final long actualTime = Instant.now().toEpochMilli() - this.timeStart;

        this.notesPlaying = getPlayingNotes(actualTime);

        //drawing of each note
        if (!this.notesPlaying.isEmpty()) {
            for (final Note n : this.notesPlaying) {
                drawNote(n.getPosxNote(), n.getPosyNote());
                this.deltaTime = Gdx.graphics.getDeltaTime();
                n.updateNote(this.deltaTime);
                this.keyboard = new EventsFromKeyboardImpl(n);
                this.key = new KeyImpl(n, actualTime);


                //set the sparks
                if (this.keyboard.isColumnSelected(this.numberOfColumns)) {
                    isSparked(n);
                }

                /*//it returns the time when the user starts to press a key
                if (this.keyboard.isColumnSelected() && !n.isPressed()) { 
                    n.setIsPressed(true);
                    this.key.startPressing();
                }*/

                /*//it returns the time when the user finishes to press a key
                if (!this.keyboard.isColumnSelected() && n.isPressed()) {
                    n.setIsPressed(false);
                    this.key.finishPressing();
                }*/


                /*//tempo di caduta
                if (n.getTimeOfFall() > 0) {
                    System.out.println("nota " + n.getColumn().name() + "tempo di caduta " + n.getTimeOfFall());
                }*/

                //change the style of the buttons if they are clicked
                for (int i = 0; i < this.numberOfColumns; i++) {
                    if (!this.keyboard.isButtonPressed(i + 1, this.numberOfColumns)) {
                        this.buttons.get(i).setStyle(this.styleUp);
                    } else {
                        this.buttons.get(i).setStyle(this.styleDown);
                    }
                }

            }
            //removal of notes that are terminated
            this.notesPlaying.removeAll(notFinished(this.notesPlaying, actualTime));
            }

        this.batch.end();
        this.backgroundBatch.end();


	}
	
	@Override
	public void dispose() {
	    this.stage.dispose();
		this.batch.dispose();
		this.background.dispose();
		this.backgroundBatch.dispose();
		this.generator.dispose();
	}
	
	
}
