package com.tamaproject.minigames;

import javax.microedition.khronos.opengles.GL10;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import com.tamaproject.BaseAndEngineGame;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;

public class TamaNinja extends BaseAndEngineGame  implements IOnSceneTouchListener {
	
	private Camera mCamera;
	
	/* This one for the font */
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	private ChangeableText score;
	
	/* This one is for all other textures */
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mPlayerTextureRegion;
	private TextureRegion mProjectileTextureRegion;
	private TextureRegion mTargetTextureRegion;
	private TextureRegion mPausedTextureRegion;
	private TextureRegion mWinTextureRegion;
	private TextureRegion mFailTextureRegion;
	
	/* The main scene for the game*/
	private Scene mMainScene;
	private Sprite player;
	
	/* Win/Fail sprite */
	private Sprite winSprite;
	private Sprite failSprite;
	
	private LinkedList<Sprite> projectileLL;
	private LinkedList<Sprite> targetLL;
	private LinkedList<Sprite> projectilesToBeAdded;
	private LinkedList<Sprite> TargetsToBeAdded;
	private Sound shootingSound;
	private Music backgroundMusic;
	private boolean runningFlag = false;
	private boolean pauseFlag = false;
	private CameraScene mPauseScene;
	private CameraScene mResultScene;
	private int hitCount;
	private final int maxScore = 2;
	
	
	@Override
	public Engine onLoadEngine() {
		/* Getting the device's screen size */
		final Display display = getWindowManager().getDefaultDisplay();
		int cameraWidth = display.getWidth();
		int cameraHeight = display.getHeight();
		
		/* Setting up the camera */
		mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
		
		/* Engine with various options */
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera).setNeedsSound(true).setNeedsMusic(true));
		
	}
	
	@Override
	public void onLoadResources() {
		/* Prepare a container for the image */
		mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		/* Prepare a container for the font */
		mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		/* Setting assets path for easy access */
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		/* Loading the image inside the container */
		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "player.png", 0, 0);
		mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "Projectile.png", 64, 0);
		mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "Target.png", 128, 0);
		mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "paused.png", 0, 64);
		mWinTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "win.png", 0, 128);
		mFailTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "fail.png", 0, 256);
		
		/* Preparing the font */
		mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 40, true, Color.BLACK);
		
		/* Loading texture in engine */
		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);
		
		SoundFactory.setAssetBasePath("mfx/");
		
		try {
			shootingSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "pew.mp3");
		} catch(IllegalStateException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		MusicFactory.setAssetBasePath("mfx/");
		try {
			backgroundMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "playing_with_power.mp3");
			backgroundMusic.setLooping(true);
		} catch(IllegalStateException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		
		/* Create a new scene for the pause menu */
		mPauseScene = new CameraScene(mCamera);
		
		/* Make the label center on the camera */
		final int x = (int) (mCamera.getWidth() / 2 - mPausedTextureRegion.getWidth() / 2);
		final int y = (int) (mCamera.getHeight() / 2 - mPausedTextureRegion.getHeight() / 2);
		final Sprite pausedSprite = new Sprite(x, y, mPausedTextureRegion);
		mPauseScene.attachChild(pausedSprite);
		
		/* Makes the scene transparent */
		mPauseScene.setBackgroundEnabled(false);
		
		/* The results scene, for win/fail */
		mResultScene = new CameraScene(mCamera);
		winSprite = new Sprite(x, y, mWinTextureRegion);
		failSprite = new Sprite(x, y, mFailTextureRegion);
		mResultScene.attachChild(winSprite);
		mResultScene.attachChild(failSprite);
		
		/* Makes the scene transparent */
		mResultScene.setBackgroundEnabled(false);
		
		winSprite.setVisible(false);
		failSprite.setVisible(false);
		
		/* Set background color */
		mMainScene = new Scene();
		mMainScene.setBackground(new ColorBackground(247 / 255f, 233 / 255f, 103 / 255f));
		mMainScene.setOnSceneTouchListener(this);
		
		/* Set coordinates for the player */
		final int PlayerX = this.mPlayerTextureRegion.getWidth() / 2;
		final int PlayerY = (int) ((mCamera.getHeight() - mPlayerTextureRegion.getHeight()) / 2);
		
		/* Set the player on scene */
		player = new Sprite(PlayerX, PlayerY, mPlayerTextureRegion);
		player.setScale(2);
		
		/* Initializing variables */
		projectileLL = new LinkedList<Sprite>();
		targetLL = new LinkedList<Sprite>();
		projectilesToBeAdded = new LinkedList<Sprite>();
		TargetsToBeAdded = new LinkedList<Sprite>();
		
		/* Setting score to the value of the max score to make sure it appears correctly on the screen */
		score = new ChangeableText(0, 0, mFont, String.valueOf(maxScore));
		
		/* Repositioning the score later so we can use the score.getWidth() */
		score.setPosition(mCamera.getWidth() - score.getWidth() - 5, 5);
		
		createSpriteSpawnTimeHandler();
		mMainScene.registerUpdateHandler(detect);
		
		/* Starte background music */
		backgroundMusic.play();
		
		restart();
		return mMainScene;
	}
	
	@Override
	public void onLoadComplete() {
		
	}
	
	/* TimerHandler for collision detection and cleaning up */
	IUpdateHandler detect = new IUpdateHandler() {
		@Override
		public void reset() {
			
		}
		@Override
		public void onUpdate(float pSecondsElapsed) {
			Iterator<Sprite> targets = targetLL.iterator();
			Sprite _target;
			boolean hit = false;
			
			/* Iterate over the targets */
			while(targets.hasNext()) {
				_target = targets.next();
				
				/* If target passed the left edge of the screen, then remove it and call a fail */
				if(_target.getX() <= -_target.getWidth()) {
					removeSprite(_target, targets);
					fail();
					break;
				}
				Iterator<Sprite> projectiles = projectileLL.iterator();
				Sprite _projectile;
				while(projectiles.hasNext()) {
					_projectile = projectiles.next();
					
					/* in case the projectile left the screen */
					if(_projectile.getX() >= mCamera.getWidth() || _projectile.getY() >= mCamera.getHeight() + _projectile.getHeight()
							 ||_projectile.getY() <= -_projectile.getHeight()) {
						removeSprite(_projectile, projectiles);
						continue;
					}
					/* If the targets collide with a projectile, remove the projectile and set the hit flag to true */
					if(_target.collidesWith(_projectile)) {
						removeSprite(_projectile, projectiles);
						hit = true;
						break;
					}
				}
				
				/* If a projectile hit the target, remove the target, increment the hit count, and update the score */
				if(hit) {
					removeSprite(_target, targets);
					hit = false;
					hitCount++;
					score.setText(String.valueOf(hitCount));
				}
			}
			/* if max score then we are done */
			if(hitCount >= maxScore) {
				win();
			}
			
			projectileLL.addAll(projectilesToBeAdded);
			projectilesToBeAdded.clear();
			
			targetLL.addAll(TargetsToBeAdded);
			TargetsToBeAdded.clear();
		}
	};
	
	/** 
	 * Safely detach the sprite from the scene and remove it from the iterator
	 * 
	 * @param _sprite
	 * @param it
	 */
	public void removeSprite(final Sprite _sprite, Iterator<Sprite> it) {
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				mMainScene.detachChild(_sprite);
			}
		});
		it.remove();
	}
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		/* if the user tapped the screen */
		if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			final float touchX = pSceneTouchEvent.getX();
			final float touchY = pSceneTouchEvent.getY();
			shootProjectile(touchX, touchY);
			return true;
		}
		return false;
	}
	
	private void shootProjectile(final float pX, final float pY) {
		int offX = (int) (pX - player.getX());
		int offY = (int) (pY - player.getY());
		if(offX <= 0)
			return;
		
		final Sprite projectile;
		
		/* Position the projectile on the player */
		projectile = new Sprite(player.getX(), player.getY(), mProjectileTextureRegion.deepCopy());
		mMainScene.attachChild(projectile, 1);
		
		int realX = (int) (mCamera.getWidth() + projectile.getWidth() / 2.0f);
		float ratio = (float) offY / (float) offX;
		int realY = (int) ((realX * ratio) + projectile.getY());
		int offRealX = (int) (realX - projectile.getX());
		int offRealY = (int) (realY - projectile.getY());
		float length = (float) Math.sqrt((offRealX * offRealX) + (offRealY * offRealY));
		float velocity = 480.0f / 1.0f;
		float realMoveDuration = length / velocity;
		
		/* Defining a move modifier from the projectile's position to the calculated one */
		MoveModifier mod = new MoveModifier(realMoveDuration, projectile.getX(), realX, projectile.getY(), realY);
		projectile.registerEntityModifier(mod.deepCopy());
		
		projectilesToBeAdded.add(projectile);
		
		/* Plays a shooting sound */
		shootingSound.play();
	}
	
	/* Adds a target at a random location and let it move along the x-axis */
	public void addTarget() {
		Random rand = new Random();
		
		int x = (int) mCamera.getWidth() + mTargetTextureRegion.getWidth();
		int minY = mTargetTextureRegion.getHeight();
		int maxY = (int) (mCamera.getHeight() - mTargetTextureRegion.getHeight());
		int rangeY = maxY - minY;
		int y = rand.nextInt(rangeY) + minY;
		
		Sprite target = new Sprite(x, y, mTargetTextureRegion.deepCopy());
		mMainScene.attachChild(target);
		
		int minDuration = 5;
		int maxDuration = 10;
		int rangeDuration = maxDuration - minDuration;
		int actualDuration = rand.nextInt(rangeDuration) + minDuration;
		
		MoveXModifier mod = new MoveXModifier(actualDuration, target.getX(), -target.getWidth());
		target.registerEntityModifier(mod.deepCopy());
		TargetsToBeAdded.add(target);
	}
	
	/* A time handler for spawning the targets, triggers every 1 second */
	private void createSpriteSpawnTimeHandler() {
		TimerHandler spriteTimerHandler;
		float mEffectSpawnDelay = 5f;
		
		spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				addTarget();
			}
		});
		getEngine().registerUpdateHandler(spriteTimerHandler);
	}
	
	/* To restart the game and clear the whole screen */
	public void restart() {
		runOnUpdateThread(new Runnable() {
			@Override
			/*to Safely detach and reattach the sprite */
			public void run() {
				mMainScene.detachChildren();
				mMainScene.attachChild(player, 0);
				mMainScene.attachChild(score);
			}
		});
		
		/* Resetting everything */
		hitCount = 0;
		score.setText(String.valueOf(hitCount));
		projectileLL.clear();
		projectilesToBeAdded.clear();
		targetLL.clear();
	}
	
	@Override
	/**
	 *  Pauses the music and the game when the game goes to the background */
	protected void onPause() {
		if(runningFlag) {
			pauseMusic();
			if(mEngine.isRunning()) {
				pauseGame();
				pauseFlag = true;
			}
		}
		super.onPause();
	}
	
	@Override
	public void onResumeGame() {
		super.onResumeGame();
		/* Shows this toast when coming back to the game */
		if(runningFlag) {
			if(pauseFlag) {
				pauseFlag = false;
				Toast.makeText(this, "Menu button to resume", Toast.LENGTH_SHORT).show();
			} else {
				/* In case the user click the home button while the game on the resultScene */
				resumeMusic();
				mEngine.stop();
			}
		}
		else {
			runningFlag = true;
		}
	}
	
	public void pauseMusic() {
		if (runningFlag)
			if (backgroundMusic.isPlaying())
				backgroundMusic.pause();
	}
	
	public void resumeMusic() {
		if(runningFlag)
			if(!backgroundMusic.isPlaying())
				backgroundMusic.resume();
	}
	
	public void fail() {
		if(mEngine.isRunning()) {
			winSprite.setVisible(false);
			failSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}
	
	public void win() {
		if(mEngine.isRunning()) {
			failSprite.setVisible(false);
			winSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}
	
	public void pauseGame() {
		if(runningFlag) {
			mMainScene.setChildScene(mPauseScene, false, true, true);
			mEngine.stop();
		}
	}
	
	public void unPauseGame() {
		mMainScene.clearChildScene();
	}
	
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		/* if menu button is pressed */
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(mEngine.isRunning() && backgroundMusic.isPlaying()) {
				pauseMusic();
				pauseFlag = true;
				pauseGame();
				Toast.makeText(this, "Menu Button to reume", Toast.LENGTH_SHORT).show();
			}
			else {
				if(!backgroundMusic.isPlaying()) {
					unPauseGame();
					pauseFlag = false;
					resumeMusic();
					mEngine.start();
				}
				return true;
			}
		}
		/* if back key was pressed */
		else if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(!mEngine.isRunning() && backgroundMusic.isPlaying()) {
				mMainScene.clearChildScene();
				mEngine.start();
				restart();
				return true;
			}
			return super.onKeyDown(pKeyCode, pEvent);
		}
		return super.onKeyDown(pKeyCode, pEvent);
	}

	@Override
	public void pauseSound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resumeSound() {
		// TODO Auto-generated method stub
		
	}
}