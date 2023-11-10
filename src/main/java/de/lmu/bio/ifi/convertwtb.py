import struct
import glob
import os

def convert_wtb_to_csv(csv_file):
    with open(csv_file, 'w') as f_out:
        for wtb_file in glob.glob('*.wtb'):
            with open(wtb_file, 'rb') as f_in:
                # Skip the 16-byte header
                f_in.read(16)

                while True:
                    # Read a game record
                    data = f_in.read(68)
                    if len(data) < 68:
                        break

                    # Unpack the data
                    tournament, player_black, player_white, score_black, score_theoretical, moves = struct.unpack('<HHHBB60s', data)

                    # Convert the winner to the desired format
                    if score_black > 32:
                        winner = 1
                    elif score_black < 32:
                        winner = -1
                    else:
                        winner = 0

                    # Convert the moves to the desired format
                    moves = [f'{chr((move // 10) + ord("a") - 1)}{move % 10}' for move in moves if move != 0]

                    # Write to the CSV file
                    f_out.write(f',{winner},{"".join(moves)}\n')

# Usage
convert_wtb_to_csv('output.csv')
