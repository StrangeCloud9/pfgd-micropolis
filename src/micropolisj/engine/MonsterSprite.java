// This file is part of MicropolisJ.
// Copyright (C) 2013 Jason Long
// Portions Copyright (C) 1989-2007 Electronic Arts Inc.
//
// MicropolisJ is free software; you can redistribute it and/or modify
// it under the terms of the GNU GPLv3, with additional terms.
// See the README file, included in this distribution, for details.

package micropolisj.engine;

import static micropolisj.engine.TileConstants.*;

/**
 * Implements a monster (one of the Micropolis disasters).
 */
public class MonsterSprite extends Sprite
{
	int count;
	int soundCount;
	int destX;
	int destY;
	int origX;
	int origY;
	int step;
	int directChangeCnt;
	boolean flag; //true if the monster wants to return home
	int curDirec;

	//GODZILLA FRAMES
	//   1...3 : northeast
	//   4...6 : southeast
	//   7...9 : southwest
	//  10..12 : northwest
	//      13 : north
	//      14 : east
	//      15 : south
	//      16 : west

	// movement deltas
	static int [] Gx = { 2, 2, -2, -2, 0 };
	static int [] Gy = { -2, 2, 2, -2, 0 };
	
	static int [] ND1 = {  0, 1, 2, 3 };
	static int [] ND2 = {  1, 2, 3, 0 };
	static int [] nn1 = {  2, 5, 8, 11 };
	static int [] nn2 = { 11, 2, 5,  8 };
	
	static int [] dx = {1, 1, -1, -1, 0, 1, 0, -1};
	static int [] dy = {-1, 1, 1, -1, -1, 0, 1, 0};
	
//	static int [] dx = {0, 0, 1, -1, 1, 1, -1, -1};
//	static int [] dy = {1, -1, 0, 0, 1, -1, 1, -1};
	public boolean isWire(int xpos, int ypos) {
		boolean isWireFlag = false;
		int c = getChar(xpos, ypos);
		for(int i = 0; i < WireTable.length; i++) {
			if(WireTable[i] == c) 
				isWireFlag = true;
		}
		return isWireFlag && city.hasPower(xpos / 16, ypos / 16);

	}
	public MonsterSprite(Micropolis engine, int xpos, int ypos)
	{
		super(engine, SpriteKind.GOD);
		this.x = xpos * 16 + 8;
		this.y = ypos * 16 + 8;
		this.width = 48;
		this.height = 48;
		this.offx = -24;
		this.offy = -24;

		this.origX = x;
		this.origY = y;

		this.frame = xpos > city.getWidth() / 2 ?
			(ypos > city.getHeight() / 2 ? 10 : 7) :
			(ypos > city.getHeight() / 2 ? 1 : 4);
		//frame is used to decide the direction of the monster
		this.count = 1000;
		
		CityLocation p = city.getLocationOfMaxPollution();
		this.destX = p.x * 16 + 8;
		this.destY = p.y * 16 + 8;
		this.flag = false;
		this.step = 1;
		
		this.directChangeCnt = 5;
		this.curDirec = city.PRNG.nextInt(11) % 5;
	}
	
	//GODZILLA FRAMES
	//   1...3 : northeast
	//   4...6 : southeast
	//   7...9 : southwest
	//  10..12 : northwest
	//      13 : north
	//      14 : east
	//      15 : south
	//      16 : west
	public void changeDirectionRandomly() {
		this.curDirec = city.PRNG.nextInt(8);
		if(curDirec < 4) {//
			if(curDirec == 0) this.frame = 1;
			if(curDirec == 1) this.frame = 4;
			if(curDirec == 2) this.frame = 7;
			if(curDirec == 3) this.frame = 10;
		} else {//north east, south, west
			
			if(curDirec == 4) this.frame = 13;
			if(curDirec == 5) this.frame = 14;
			if(curDirec == 6) this.frame = 15;
			if(curDirec == 7) this.frame = 16;
			this.directChangeCnt = 10;
		}
	}
	@Override
	public void moveImpl() {
		//move it;
		
		
		if(this.frame < 13) {
			//move to next step
			int nextD = (this.frame - 1) / 3;
			int nextX = this.x + dx[nextD] * 2;
			int nextY = this.y + dy[nextD] * 2;
			boolean isWireFlag = isWire(nextX, nextY);
			if(isWireFlag || getChar(nextX, nextY) == -1) {
				// if it is the bound or the powered wire, change direction.
				changeDirectionRandomly();
				return;
			}
			this.x = nextX;
			this.y = nextY;
			//destroy everything else
			int c = getChar(x, y);
	
			for (Sprite s : city.allSprites())
			{
				if (checkSpriteCollision(s) &&
					(s.kind == SpriteKind.AIR ||
					 s.kind == SpriteKind.COP ||
					 s.kind == SpriteKind.SHI ||
					 s.kind == SpriteKind.TRA)
					) {
					s.explodeSprite();
				}
			}
	
			destroyTile(x / 16, y / 16);
			
		}
		this.directChangeCnt --;
		if(this.directChangeCnt <= 0) {
			//its time to change direction
			this.directChangeCnt = 100;
			changeDirectionRandomly();
		} else {
			//follow the current direction
			if(this.frame <= 12) {
				if(this.frame % 3 == 0) {
					this.frame -= 1;
				} else {
					this.frame ++;
				}
			}
		}
		return;
	}
}
