/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Fauzan
 */
public class Player extends GameObject{
    int num;
    public Player(int x, int y, ID id, int num, int size){
        super(x, y, id, num, size);
        //speed = 1;
    }

    @Override
    public void tick() {
        x += vel_x;
        y += vel_y;
        
        x = Game.clamp(x, 0, Game.WIDTH - 70);
        y = Game.clamp(y, 0, Game.HEIGHT - 90);

    }

    @Override
    public void render(Graphics g) {
        if(num == 0)
            g.setColor(Color.decode("#3f6082"));
        else
            g.setColor(Color.ORANGE);
        g.fillRect(x, y, 50, 50);
    }
}
