import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.Minim; 
import ddf.minim.AudioPlayer; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Minesweeper extends PApplet {




Minim min;
AudioPlayer win, tick, lose;

float offset_y = 100.0f;

int count           = 0;
int number_of_bombs = 40;
int tile_size       = 30;
int seconds         = 0;

enum State {Move, Stop};

ArrayList <Integer[]> bombs_coords = new ArrayList<Integer[]>();
ArrayList <Integer[]> flag_coords  = new ArrayList<Integer[]>();

PImage[] images = new PImage[15];

Tile[][] tiles = new Tile[16][16];

State state = State.Move;

public void setup (){
    
    loadImages();  
    _resize (tile_size, tile_size); 
    set_music();
    _start();
    prob();
}

public void draw (){
    if (state == State.Move)
    {
        if (won())
          {win();}
    }
    else if (state == State.Stop)
    {
      noLoop();
    }
}

public void _clear (){
    bombs_coords = new ArrayList<Integer[]>();
    flag_coords  = new ArrayList<Integer[]>();
    tiles        = new Tile[16][16];
}

public boolean contains (ArrayList<Integer[]> list, Integer[] arr){
    for (Integer[] a : list)
        if (a[0] == arr[0] && a[1] == arr[1])
            return true;
    return false;
}

public void draw_tiles(){
  for (int i = 0; i < tiles.length; i++){
    for (int j = 0; j < tiles[0].length; j++)
      if (tiles[i][j] != null)
        tiles[i][j]._draw();
  }  
}

public void enumerate_tiles (){
    for (int i = 0; i < tiles.length; i++){
        for (int j = 0; j < tiles[0].length; j++){
            if (! contains (bombs_coords, new Integer[]{i, j}))
            {
                if (i == 0)
                {
                    if (j == 0)
                      {increment_count(i, j + 1); increment_count(i + 1, j); increment_count(i + 1, j + 1);}
                    else if (j == 15)
                      {increment_count(i, j - 1); increment_count(i + 1, j); increment_count(i + 1, j - 1);}
                    else
                      {
                       increment_count(i, j - 1);
                       increment_count(i + 1, j - 1);
                       increment_count(i + 1, j);
                       increment_count(i + 1, j + 1);
                       increment_count(i, j + 1);
                      }
                }
                else if (i == 15)
                {
                    if (j == 0)
                      {increment_count(i - 1, j); increment_count(i - 1, j + 1); increment_count(i, j + 1);}
                    else if (j == 15)
                      {increment_count(i - 1, j); increment_count(i - 1, j - 1); increment_count(i, j - 1);}
                    else
                      {
                        increment_count(i, j - 1);
                        increment_count(i - 1, j - 1);
                        increment_count(i - 1, j);
                        increment_count(i - 1, j + 1);
                        increment_count(i, j + 1);
                      }
                }
                else if (j == 0)
                    {
                      increment_count(i - 1, j);
                      increment_count(i - 1, j + 1);
                      increment_count(i, j + 1);
                      increment_count(i + 1, j + 1);
                      increment_count(i + 1, j);
                    }
               else if (j == 15)
                   {
                      increment_count(i - 1, j);
                      increment_count(i - 1, j - 1);
                      increment_count(i, j - 1);
                      increment_count(i + 1, j - 1);
                      increment_count(i + 1, j);
                   }
              else
                  {
                      increment_count(i - 1, j);
                      increment_count(i - 1, j - 1);
                      increment_count(i, j - 1);
                      increment_count(i + 1, j - 1);
                      increment_count(i + 1, j);
                      increment_count(i + 1, j + 1);
                      increment_count(i, j + 1);
                      increment_count(i - 1, j + 1);
                  }
                
              tiles[i][j] = new Tile (i, j, count);
              count       = 0;
            }
        }
    }
}

public void print_list (ArrayList<Integer[]> list){
    for (Integer[] arr : list)
        println (arr[0] + " " + arr[1]);
}

public void mouseReleased (){
    int[] temp = point_to_grid(mouseX, mouseY);

    if (state == State.Stop           || 
        !is_in_grid(temp[0], temp[1]) ||
        !tiles[temp[0]][temp[1]].hidden)
      return;
      
    if (mouseButton == RIGHT)
    {   
        tick.play();
        tick.rewind();
        
        int a = temp[0], b = temp[1];

        if (tiles[a][b] != null)
        {   
            if (tiles[a][b].img == images[12])
                {
                  tiles[a][b].img = images[10]; 
                  flag_coords.add(new Integer[]{a, b});
                }
            else if (tiles[a][b].img == images[10])
                {              
                  tiles[a][b].img = images[11]; 
                  _remove(flag_coords, new Integer[]{a, b});                
                }
            else if (tiles[a][b].img == images[11])
                {
                  tiles[a][b].img = images[12];
                }
            
            tiles[a][b]._draw();
        }      
    }
    else if (mouseButton == LEFT)
    {   
        if (!(tiles[temp[0]][temp[1]].img == images[10]) &&
            !(tiles[temp[0]][temp[1]].img == images[11]))
            {
            if (contains(bombs_coords, new Integer[]{temp[0], temp[1]}))
                kill (temp[0], temp[1]);
            else
              {
                tick.play();
                tick.rewind();
                open (temp[0], temp[1]);  
              }
            }
    }
}

public float[] grid_to_point (int a, int b){
    float x = b * tile_size;
    float y = offset_y + a * tile_size;
    
    return new float[]{x, y};
}

public void hide_tiles (){
    for (int i = 0; i < tiles.length; i++){
        for (int j = 0; j < tiles[0].length; j++){
            if (tiles[i][j] != null)
              {  
                tiles[i][j].img    = images[12];
                tiles[i][j].hidden = true;
              }
        }
    }
    
    draw_tiles();
}

public void increment_count (int a, int b){
    if (contains(bombs_coords, new Integer[]{a, b}))
        count++;
}

public boolean is_in_grid (int a, int b){
    if (((a >= 0 && a <= 15) && (b >= 0 && b <= 15)))
      return true;
    return false;
}

public void kill (int a, int b){
    state = State.Stop;
    
    lose.play();
    textSize(24);
    fill(0);
    text ("You Lose !!!", 180, 50);
    
    for (Integer[] arr : bombs_coords)
    {  
        if (!contains(flag_coords, arr))
        {
            if (arr[0] == a && arr[1] == b)
              tiles[arr[0]][arr[1]].img = images[9];
            else
              tiles[arr[0]][arr[1]].img = images[13];
            
            tiles[arr[0]][arr[1]]._draw();
        }
    }
    show_false_flags();
}

public void prob (){
    line (0, 100, 480, 100);
}

public void loadImages (){
    for (int i = 0; i < 15; i++){
        images[i] = loadImage ("assets/images/tile" + i + ".png");
    }  
}

public void open (int a, int b){
    if (tiles[a][b] != null)
        {
          tiles[a][b].display_value();
          tiles[a][b].hidden = false;
       
          if (tiles[a][b].value == 0)
            {
                if (a != 0 && tiles[a - 1][b].hidden)
                    open (a - 1, b);  
                if (a != 15 && tiles[a + 1][b].hidden)
                    open (a + 1, b);
                if (b != 0 && tiles[a][b - 1].hidden)
                    open (a, b - 1);
                if (b != 15 && tiles[a][b + 1].hidden)
                    open (a, b + 1);
                    
                if (a != 0 && b != 0 && tiles[a - 1][b - 1].hidden)
                    open (a - 1, b - 1);
                if (a != 15 && b != 0 && tiles[a + 1][b - 1].hidden)
                    open (a + 1, b - 1);
                if (a != 0 && b != 15 && tiles[a - 1][b + 1].hidden)
                    open (a - 1, b + 1);
                if (a != 15 && b != 15 && tiles[a + 1][b + 1].hidden)
                    open (a + 1, b + 1);
            }
        }
}

public void plant_bombs () {
  int a, b;
  while (number_of_bombs > 0) {
      a = PApplet.parseInt (random (0, 16));
      b = PApplet.parseInt (random (0, 16));
      
      if (!contains(bombs_coords, new Integer[]{a, b}))
      {
        bombs_coords.add(new Integer[]{a, b});
        tiles[a][b] = new Tile (a, b, 13);
        number_of_bombs--;
      } 
  }
}

public int[] point_to_grid (float x, float y){
    
    if (y < 100)
        return new int[]{-1, -1};
    
    int a = PApplet.parseInt (x / tile_size);
    int b = PApplet.parseInt ((y - offset_y) / tile_size);
    
    return new int[]{b, a};
}

public void _remove (ArrayList<Integer[]> list, Integer[] arr){
    Integer[] temp = null;
    for (Integer[] a : list)
      if (a[0] == arr[0] && a[1] == arr[1])
          temp = a;
    list.remove(temp);
}
public void _resize (int _width, int _height) {
    for (int i = 0; i < images.length; i++){
        images[i].resize(_width, _height);
    }
}

public void set_music (){
    min = new Minim(this);
    win = min.loadFile("assets/music/win.wav");
    tick = min.loadFile("assets/music/tick.wav");
    lose = min.loadFile("assets/music/lose.wav");
}

public void show_false_flags(){
    for (Integer[] arr : flag_coords)
        {
          if (!contains(bombs_coords, arr))
            {
              tiles[arr[0]][arr[1]].img = images[14];
              tiles[arr[0]][arr[1]]._draw();
            }
        }
}

public void _start (){
    state = State.Move;
    
    plant_bombs();
    enumerate_tiles();
    hide_tiles();
    draw_tiles();
}

public void win (){
    win.play(); 
    state = State.Stop; 
    textSize(24);
    fill(0);
    text ("You Win!!!", 180, 50);
}

public boolean won (){
    for (int i = 0; i < tiles.length; i++){
        for (int j = 0; j < tiles[0].length; j++){
            if (tiles[i][j] != null && !contains(bombs_coords, new Integer[]{i, j}) &&
                tiles[i][j].hidden)
              return false;
        }
    }
    
    return true;
}

class Tile {
  
  float p_x, p_y;
  int   g_x, g_y;
  
  int value;
  
  PImage img;
  
  boolean hidden = false;
  
  Tile (int g_x, int g_y, int value){
      this.g_x   = g_x;
      this.g_y   = g_y;
      this.value = value;
      
      img = images[value];
      
      float[] temp = grid_to_point(g_x, g_y);
      p_x = temp[0];
      p_y = temp[1];
  }
  
  public void _draw (){
      image(img, p_x, p_y);
  }
  
  public void display_value (){
    img = images[value];
    _draw();
  }
}
  public void settings() {  size (480, 580); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Minesweeper" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
