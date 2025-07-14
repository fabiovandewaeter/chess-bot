import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

# Données du benchmark
data = {
    'BotLevel': [5],
    'StockfishLevel': [10],
    'WinRate': [0,00]
}

df = pd.DataFrame(data)

# Graphique en barres
plt.figure(figsize=(12, 8))
for bot_level in sorted(df['BotLevel'].unique()):
    subset = df[df['BotLevel'] == bot_level]
    plt.plot(subset['StockfishLevel'], subset['WinRate'], 
             marker='o', label=f'Bot Level {bot_level}')

plt.xlabel('Niveau Stockfish')
plt.ylabel('Taux de victoire du bot (%)')
plt.title('Performance du bot vs Stockfish')
plt.legend()
plt.grid(True, alpha=0.3)
plt.show()

# Heatmap
pivot_table = df.pivot(index='BotLevel', columns='StockfishLevel', values='WinRate')
plt.figure(figsize=(10, 6))
plt.imshow(pivot_table.values, cmap='RdYlGn', aspect='auto')
plt.colorbar(label='Taux de victoire (%)')
plt.xlabel('Niveau Stockfish')
plt.ylabel('Niveau Bot')
plt.title('Heatmap des performances')
plt.xticks(range(len(pivot_table.columns)), pivot_table.columns)
plt.yticks(range(len(pivot_table.index)), pivot_table.index)
plt.show()
