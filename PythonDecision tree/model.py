import pandas as pd
import numpy as np
import os

MAX_DEPTH = 10
PREDICTION_NUM = 20
TRAIN_RATIO = 0.8

df = pd.read_csv("cleaned_data.csv")

X = df.drop(columns=['cost'])  # Remove the target variable
y = df['cost']  # Target variable (cost)


# Calculate split index
split_index = int(len(df) * TRAIN_RATIO)

# Split the data manually
train_df = df.iloc[:split_index]  # First 80% for training
test_df = df.iloc[split_index:]   # Last 20% for testing


if os.path.exists("train_data.csv"):
    os.remove("train_data.csv")

if os.path.exists("test_data.csv"):
    os.remove("test_data.csv")

train_df.to_csv("train_data.csv", index=False)
print("Training data saved as 'train_data.csv'")

# Save testing data
test_df.to_csv("test_data.csv", index=False)
print("Testing data saved as 'test_data.csv'")


X_train = train_df.drop(columns=['cost']).values
y_train = train_df['cost'].values

X_test = test_df.drop(columns=['cost']).values
y_test = test_df['cost'].values

print(f"Data Loaded: Training Set = {X_train.shape}, Testing Set = {X_test.shape}")


class DecisionTreeNode:
    def __init__(self, feature=None, threshold=None, left=None, right=None, prediction=None):
        self.feature = feature
        self.threshold = threshold
        self.left = left 
        self.right = right
        self.prediction = prediction


def gini_impurity(y):
    unique_classes, counts = np.unique(y, return_counts=True)
    probabilities = counts / counts.sum()
    return 1 - np.sum(probabilities**2)

def find_best_split(X, y):
    best_gini = 999  
    best_feature = None
    best_threshold = None

    for feature_index in range(X.shape[1]):
        thresholds = np.unique(X[:, feature_index])

        for threshold in thresholds:
            left_mask = X[:, feature_index] <= threshold
            right_mask = ~left_mask

            if np.sum(left_mask) == 0 or np.sum(right_mask) == 0:
                continue  # Skip empty splits

            gini_left = gini_impurity(y[left_mask])
            gini_right = gini_impurity(y[right_mask])

            gini_weighted = (np.sum(left_mask) * gini_left + np.sum(right_mask) * gini_right) / len(y)

            if gini_weighted < best_gini:
                best_gini = gini_weighted
                best_feature = feature_index
                best_threshold = threshold

    return best_feature, best_threshold


def build_tree(X, y, depth=0, max_depth=5):
    if depth >= max_depth or len(set(y)) == 1: #if depth is equal to max or All Labels Same create leaf node
        return DecisionTreeNode(prediction=np.mean(y))

    feature, threshold = find_best_split(X, y)

    if feature is None:
        return DecisionTreeNode(prediction=np.mean(y))  # If no valid split, return leaf node

    left_mask = X[:, feature] <= threshold
    right_mask = ~left_mask

    left_subtree = build_tree(X[left_mask], y[left_mask], depth + 1, max_depth)
    right_subtree = build_tree(X[right_mask], y[right_mask], depth + 1, max_depth)

    return DecisionTreeNode(feature, threshold, left_subtree, right_subtree)

decision_tree = build_tree(X_train, y_train,0, MAX_DEPTH)

def predict(tree, x):
    if tree.prediction is not None:
        return tree.prediction  # If it's a leaf node, return prediction

    if x[tree.feature] <= tree.threshold:
        return predict(tree.left, x)
    else:
        return predict(tree.right, x) 
    
def mean_absolute_error_manual(y_true, y_pred):
    total_error = 0
    for true, pred in zip(y_true, y_pred):
        total_error += abs(true - pred)
    return total_error / len(y_true)

# Make predictions on the test set
y_pred = np.array([predict(decision_tree, x) for x in X_test])

# Compute MAE manually
mae_manual = mean_absolute_error_manual(y_test, y_pred)
print(f" Manual MAE: {mae_manual:.2f}")

def r2_score_manual(y_true, y_pred):
    mean_y = sum(y_true) / len(y_true)
    
    ss_total = sum((y - mean_y) ** 2 for y in y_true)  # Total variance
    ss_residual = sum((y_true[i] - y_pred[i]) ** 2 for i in range(len(y_true)))  # Residual variance
    
    return 1 - (ss_residual / ss_total)

# Compute R² manually
r2_manual = r2_score_manual(y_test, y_pred)
print(f"R² Score: {r2_manual:.2f}")

print("\n Sample Predictions:")
actual_prices = test_df['cost'].values  # Get actual cost values from test dataset

for i in range(PREDICTION_NUM):  # Print first 5 predictions
    print(f"Actual Price: ${actual_prices[i]:.2f}, Predicted Price: ${y_pred[i]:.2f}")