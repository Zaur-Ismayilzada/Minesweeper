import ddf.minim.Minim;
import ddf.minim.AudioPlayer;

Minim min;
AudioPlayer win, tick, lose;

float offset_y = 100.0;

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

void setup (){
    size (480, 580);
    loadImages();  
    _resize (tile_size, tile_size); 
    set_music();
    _start();
    prob();
}

void draw (){
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

void _clear (){
    bombs_coords = new ArrayList<Integer[]>();
    flag_coords  = new ArrayList<Integer[]>();
    tiles        = new Tile[16][16];
}

boolean contains (ArrayList<Integer[]> list, Integer[] arr){
    for (Integer[] a : list)
        if (a[0] == arr[0] && a[1] == arr[1])
            return true;
    return false;
}

void draw_tiles(){
  for (int i = 0; i < tiles.length; i++){
    for (int j = 0; j < tiles[0].length; j++)
      if (tiles[i][j] != null)
        tiles[i][j]._draw();
  }  
}

void enumerate_tiles (){
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

void print_list (ArrayList<Integer[]> list){
    for (Integer[] arr : list)
        println (arr[0] + " " + arr[1]);
}

void mouseReleased (){
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

float[] grid_to_point (int a, int b){
    float x = b * tile_size;
    float y = offset_y + a * tile_size;
    
    return new float[]{x, y};
}

void hide_tiles (){
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

void increment_count (int a, int b){
    if (contains(bombs_coords, new Integer[]{a, b}))
        count++;
}

boolean is_in_grid (int a, int b){
    if (((a >= 0 && a <= 15) && (b >= 0 && b <= 15)))
      return true;
    return false;
}

void kill (int a, int b){
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

void prob (){
    line (0, 100, 480, 100);
}

void loadImages (){
    for (int i = 0; i < 15; i++){
        images[i] = loadImage ("assets/images/tile" + i + ".png");
    }  
}

void open (int a, int b){
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

void plant_bombs () {
  int a, b;
  while (number_of_bombs > 0) {
      a = int (random (0, 16));
      b = int (random (0, 16));
      
      if (!contains(bombs_coords, new Integer[]{a, b}))
      {
        bombs_coords.add(new Integer[]{a, b});
        tiles[a][b] = new Tile (a, b, 13);
        number_of_bombs--;
      } 
  }
}

int[] point_to_grid (float x, float y){
    
    if (y < 100)
        return new int[]{-1, -1};
    
    int a = int (x / tile_size);
    int b = int ((y - offset_y) / tile_size);
    
    return new int[]{b, a};
}

void _remove (ArrayList<Integer[]> list, Integer[] arr){
    Integer[] temp = null;
    for (Integer[] a : list)
      if (a[0] == arr[0] && a[1] == arr[1])
          temp = a;
    list.remove(temp);
}
void _resize (int _width, int _height) {
    for (int i = 0; i < images.length; i++){
        images[i].resize(_width, _height);
    }
}

void set_music (){
    min = new Minim(this);
    win = min.loadFile("assets/music/win.wav");
    tick = min.loadFile("assets/music/tick.wav");
    lose = min.loadFile("assets/music/lose.wav");
}

void show_false_flags(){
    for (Integer[] arr : flag_coords)
        {
          if (!contains(bombs_coords, arr))
            {
              tiles[arr[0]][arr[1]].img = images[14];
              tiles[arr[0]][arr[1]]._draw();
            }
        }
}

void _start (){
    state = State.Move;
    
    plant_bombs();
    enumerate_tiles();
    hide_tiles();
    draw_tiles();
}

void win (){
    win.play(); 
    state = State.Stop; 
    textSize(24);
    fill(0);
    text ("You Win!!!", 180, 50);
}

boolean won (){
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
  
  void _draw (){
      image(img, p_x, p_y);
  }
  
  void display_value (){
    img = images[value];
    _draw();
  }
}
